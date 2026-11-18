package com.youtubeagent.service;

import com.youtubeagent.model.*;
import com.youtubeagent.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@Service
public class FolderMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(FolderMonitoringService.class);

    private final VideoFileRepository videoFileRepository;
    private final VideoMetadataRepository metadataRepository;
    private final PublishingQueueRepository queueRepository;
    private final AutomationSettingRepository settingRepository;
    private final VideoValidationService validationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final GeminiMetadataService geminiMetadataService;
    private final AutomationLogService logService;
    private final NotificationRepository notificationRepository;

    @Value("${agent.folder.monitored-path:C:\\Users\\vijay\\OneDrive\\Desktop\\Youtube Agent\\Clips}")
    private String defaultMonitoredPath;

    public FolderMonitoringService(VideoFileRepository videoFileRepository,
                                   VideoMetadataRepository metadataRepository,
                                   PublishingQueueRepository queueRepository,
                                   AutomationSettingRepository settingRepository,
                                   VideoValidationService validationService,
                                   DuplicateDetectionService duplicateDetectionService,
                                   GeminiMetadataService geminiMetadataService,
                                   AutomationLogService logService,
                                   NotificationRepository notificationRepository) {
        this.videoFileRepository = videoFileRepository;
        this.metadataRepository = metadataRepository;
        this.queueRepository = queueRepository;
        this.settingRepository = settingRepository;
        this.validationService = validationService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.geminiMetadataService = geminiMetadataService;
        this.logService = logService;
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void init() {
        String path = getEffectiveMonitoredFolder();
        logService.info("MONITOR", "Folder Monitoring Service started. Monitored path: " + path);
        // Initial scan on startup
        scanFolderNow();
    }

    public String getEffectiveMonitoredFolder() {
        return settingRepository.findBySettingKey("MONITORED_FOLDER_PATH")
                .map(AutomationSetting::getSettingValue)
                .filter(s -> !s.isBlank())
                .orElse(defaultMonitoredPath);
    }

    public void updateMonitoredFolder(String newPath) {
        if (newPath == null || newPath.isBlank()) return;
        AutomationSetting setting = settingRepository.findBySettingKey("MONITORED_FOLDER_PATH")
                .orElse(new AutomationSetting("MONITORED_FOLDER_PATH", newPath, "Designated video folder path"));
        setting.setSettingValue(newPath);
        setting.setUpdatedAt(LocalDateTime.now());
        settingRepository.save(setting);
        logService.info("MONITOR", "Monitored folder path updated to: " + newPath);
        scanFolderNow();
    }

    /**
     * Periodic background scanner (runs every 10 seconds).
     */
    @Scheduled(fixedDelayString = "${agent.folder.scan-interval-seconds:10}000")
    public void scheduledScan() {
        scanFolderNow();
    }

    /**
     * Scans folder for videos, validates them, prevents duplicates, generates AI metadata, and adds to queue.
     */
    public synchronized int scanFolderNow() {
        String folderPath = getEffectiveMonitoredFolder();
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!folder.isDirectory()) {
            logService.warn("SCAN", "Monitored folder is not a directory: " + folderPath);
            return 0;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }

        // Sort by file name for deterministic discovery
        Arrays.sort(files, Comparator.comparing(File::getName));

        int newVideosRegistered = 0;

        for (File file : files) {
            if (file.isDirectory() || file.isHidden()) {
                continue;
            }

            String filename = file.getName();
            String lower = filename.toLowerCase();
            if (!lower.endsWith(".mp4") && !lower.endsWith(".mov") && !lower.endsWith(".mkv") && !lower.endsWith(".webm")) {
                continue;
            }

            // Quick check if file path is already registered with unchanged size
            Optional<VideoFile> existingByPath = videoFileRepository.findByFilePath(file.getAbsolutePath());
            if (existingByPath.isPresent() && existingByPath.get().getFileSize().equals(file.length())) {
                continue; // Already processed
            }

            // STEP 3: File Validation
            VideoValidationService.ValidationResult validation = validationService.validate(file);
            if (!validation.isValid()) {
                logService.warn("VALIDATION", "Skipping file \"" + filename + "\": " + validation.getErrorMessage());
                continue;
            }

            String fileHash = validation.getFileHash();

            // STEP 4: Permanent Duplicate Verification
            if (duplicateDetectionService.isAlreadyPublished(fileHash)) {
                logService.warn("DUPLICATE", "Skipping video \"" + filename + "\". Permanent duplicate detected (already published to YouTube with hash: " + fileHash + ")", null, fileHash);
                continue;
            }

            if (duplicateDetectionService.isAlreadyRegistered(fileHash)) {
                continue; // Already in DB
            }

            // Register new video file
            try {
                registerNewVideo(file, validation);
                newVideosRegistered++;
            } catch (Exception e) {
                logService.error("ERROR", "Failed to register video file \"" + filename + "\": " + e.getMessage(), null, fileHash);
            }
        }

        if (newVideosRegistered > 0) {
            logService.info("DETECT", "Folder scan complete. Registered and queued " + newVideosRegistered + " new video(s).");
            notificationRepository.save(new Notification(
                    "New Videos Detected! 🎬",
                    newVideosRegistered + " new video(s) detected and added to publishing queue.",
                    "INFO"
            ));
        }

        return newVideosRegistered;
    }

    @Transactional
    protected void registerNewVideo(File file, VideoValidationService.ValidationResult validation) {
        String fileHash = validation.getFileHash();

        // 1. Create VideoFile record
        VideoFile videoFile = new VideoFile();
        videoFile.setFilename(file.getName());
        videoFile.setFilePath(file.getAbsolutePath());
        videoFile.setFileHash(fileHash);
        videoFile.setFileSize(validation.getFileSize());
        videoFile.setDuration(validation.getDuration());
        videoFile.setResolution(validation.getResolution());
        videoFile.setFormat(validation.getFormat());
        videoFile.setDetectedAt(LocalDateTime.now());
        videoFile.setStatus(VideoStatus.VALIDATING);

        VideoFile savedFile = videoFileRepository.save(videoFile);
        logService.info("DETECT", "Detected new video: " + file.getName() + " (" + validation.getResolution() + ", " + validation.getDuration() + "s)", savedFile.getId(), fileHash);

        // 2. Generate AI Metadata
        GeminiMetadataService.GeneratedMetadata meta = geminiMetadataService.generateMetadata(file.getName(), savedFile.getId(), fileHash);

        VideoMetadata videoMetadata = new VideoMetadata();
        videoMetadata.setVideoFileId(savedFile.getId());
        videoMetadata.setTitle(meta.getTitle());
        videoMetadata.setDescription(meta.getDescription());
        videoMetadata.setTags(meta.getTags());
        videoMetadata.setHashtags(meta.getHashtags());
        videoMetadata.setCategoryId("20"); // Gaming
        videoMetadata.setPrivacyStatus("public");
        videoMetadata.setIsMadeForKids(false);
        videoMetadata.setAiGenerated(meta.isAiGenerated());
        metadataRepository.save(videoMetadata);

        // 3. Add to Publishing Queue
        int nextPos = queueRepository.findMaxQueuePosition() + 1;
        PublishingQueue queue = new PublishingQueue();
        queue.setVideoFileId(savedFile.getId());
        queue.setQueuePosition(nextPos);
        queue.setStatus(VideoStatus.QUEUED);
        queue.setPriority(0);
        queueRepository.save(queue);

        savedFile.setStatus(VideoStatus.QUEUED);
        videoFileRepository.save(savedFile);

        logService.info("QUEUE", "Video added to publishing queue at position #" + nextPos + ": \"" + meta.getTitle() + "\"", savedFile.getId(), fileHash);
    }
}
