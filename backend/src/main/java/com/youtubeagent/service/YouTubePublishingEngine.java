package com.youtubeagent.service;

import com.youtubeagent.model.*;
import com.youtubeagent.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class YouTubePublishingEngine {

    private static final Logger log = LoggerFactory.getLogger(YouTubePublishingEngine.class);

    private final PublishingQueueRepository queueRepository;
    private final VideoFileRepository videoFileRepository;
    private final VideoMetadataRepository metadataRepository;
    private final PublishedVideoRepository publishedVideoRepository;
    private final PublishingJobRepository jobRepository;
    private final PublishingScheduleRepository scheduleRepository;
    private final YouTubeApiService youTubeApiService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final AutomationLogService logService;
    private final NotificationRepository notificationRepository;
    private final AutomationSettingRepository settingRepository;

    public YouTubePublishingEngine(PublishingQueueRepository queueRepository,
                                   VideoFileRepository videoFileRepository,
                                   VideoMetadataRepository metadataRepository,
                                   PublishedVideoRepository publishedVideoRepository,
                                   PublishingJobRepository jobRepository,
                                   PublishingScheduleRepository scheduleRepository,
                                   YouTubeApiService youTubeApiService,
                                   DuplicateDetectionService duplicateDetectionService,
                                   AutomationLogService logService,
                                   NotificationRepository notificationRepository,
                                   AutomationSettingRepository settingRepository) {
        this.queueRepository = queueRepository;
        this.videoFileRepository = videoFileRepository;
        this.metadataRepository = metadataRepository;
        this.publishedVideoRepository = publishedVideoRepository;
        this.jobRepository = jobRepository;
        this.scheduleRepository = scheduleRepository;
        this.youTubeApiService = youTubeApiService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.logService = logService;
        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
    }

    @PostConstruct
    public void onStartupRecovery() {
        logService.info("RESTART", "Publishing engine initialized. Checking for interrupted jobs to safely recover...");
        recoverInterruptedJobs();
    }

    /**
     * Recovers any job that was interrupted in-flight before server shutdown.
     */
    @Transactional
    public void recoverInterruptedJobs() {
        List<PublishingJob> inProgressJobs = jobRepository.findByStatus(JobStatus.IN_PROGRESS);
        for (PublishingJob job : inProgressJobs) {
            // Check if already reached YouTube
            if (publishedVideoRepository.existsByFileHash(job.getFileHash())) {
                job.setStatus(JobStatus.SUCCESS);
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);
                logService.info("RESTART", "Job " + job.getId() + " was confirmed published on YouTube.", job.getVideoFileId(), job.getFileHash());
            } else {
                job.setStatus(JobStatus.RETRY_PENDING);
                job.setErrorMessage("Application restarted during upload");
                jobRepository.save(job);

                videoFileRepository.findById(job.getVideoFileId()).ifPresent(vf -> {
                    vf.setStatus(VideoStatus.RETRY_PENDING);
                    videoFileRepository.save(vf);
                });

                queueRepository.findByVideoFileId(job.getVideoFileId()).ifPresent(q -> {
                    q.setStatus(VideoStatus.RETRY_PENDING);
                    queueRepository.save(q);
                });

                logService.warn("RESTART", "Job " + job.getId() + " was interrupted. Marked for safe retry without duplicates.", job.getVideoFileId(), job.getFileHash());
            }
        }
    }

    /**
     * Heartbeat scheduler: runs every minute to check if an hourly publishing slot is triggered.
     */
    @Scheduled(fixedRate = 60000)
    public void checkAndTriggerScheduledPublishing() {
        PublishingSchedule schedule = getOrCreateSchedule();
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            return;
        }

        ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Kolkata");
        LocalDateTime now = LocalDateTime.now(zoneId);

        // Check active daily time window
        if (!isWithinPublishingWindow(now.toLocalTime(), schedule.getStartTime(), schedule.getEndTime())) {
            return;
        }

        // Check daily publishing quota
        long todayCount = publishedVideoRepository.countPublishedToday(now.toLocalDate().atStartOfDay());
        if (todayCount >= schedule.getMaxDailyUploads()) {
            logService.info("SCHEDULE", "Daily publishing limit of " + schedule.getMaxDailyUploads() + " videos reached for today (" + todayCount + "). Pausing until tomorrow.");
            return;
        }

        // Check if next interval slot has arrived
        // For default 60 min intervals, check top of the hour (minute 0), or execute when eligible queue items exist and last run was > interval
        // To make it fully reactive to the configured interval:
        if (shouldExecuteSlot(schedule, now)) {
            executePublishingBatch(schedule.getVideosPerInterval(), "SCHEDULED_SLOT");
        }
    }

    private boolean shouldExecuteSlot(PublishingSchedule schedule, LocalDateTime now) {
        // Trigger at the start of each interval (e.g. minute == 0 for hourly)
        int interval = schedule.getIntervalMinutes() != null && schedule.getIntervalMinutes() > 0 ? schedule.getIntervalMinutes() : 60;
        int currentMinute = now.getMinute();
        return (currentMinute % interval == 0);
    }

    private boolean isWithinPublishingWindow(LocalTime nowTime, String startStr, String endStr) {
        try {
            LocalTime start = LocalTime.parse(startStr != null ? startStr : "09:00");
            LocalTime end = LocalTime.parse(endStr != null ? endStr : "23:59");
            if (start.isBefore(end)) {
                return !nowTime.isBefore(start) && !nowTime.isAfter(end);
            } else {
                // overnight window (e.g. 22:00 to 06:00)
                return !nowTime.isBefore(start) || !nowTime.isAfter(end);
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Executes publishing of exactly N videos (default 2) from queue.
     * Can be invoked by scheduler or triggered manually via "Publish Next Batch Now" button.
     */
    public synchronized int executePublishingBatch(int batchSize, String triggerSource) {
        logService.info("QUEUE", "Publishing engine triggered (" + triggerSource + "). Target batch size: " + batchSize + " videos.");

        List<PublishingQueue> eligibleQueue = queueRepository.findByStatusOrderByQueuePositionAsc(VideoStatus.QUEUED);
        if (eligibleQueue.isEmpty()) {
            // Check RETRY_PENDING items
            eligibleQueue = queueRepository.findByStatusOrderByQueuePositionAsc(VideoStatus.RETRY_PENDING);
        }

        if (eligibleQueue.isEmpty()) {
            logService.info("QUEUE", "No eligible videos in queue. Waiting for new videos to be added to the monitored folder.");
            return 0;
        }

        int publishedCount = 0;
        int target = Math.min(batchSize, eligibleQueue.size());

        for (int i = 0; i < target; i++) {
            PublishingQueue queueItem = eligibleQueue.get(i);
            boolean success = publishSingleVideo(queueItem);
            if (success) {
                publishedCount++;
            }
        }

        logService.info("PUBLISH", "Completed publishing batch. Successfully published " + publishedCount + " of " + target + " videos.");
        return publishedCount;
    }

    private boolean publishSingleVideo(PublishingQueue queueItem) {
        Long videoFileId = queueItem.getVideoFileId();
        Optional<VideoFile> vfOpt = videoFileRepository.findById(videoFileId);
        if (vfOpt.isEmpty()) {
            queueRepository.delete(queueItem);
            return false;
        }

        VideoFile videoFile = vfOpt.get();
        String fileHash = videoFile.getFileHash();

        // 1. PERMANENT DUPLICATE CHECK
        if (duplicateDetectionService.isAlreadyPublished(fileHash)) {
            logService.warn("DUPLICATE", "Skipping permanent duplicate! Hash already published: " + fileHash, videoFileId, fileHash);
            queueItem.setStatus(VideoStatus.SKIPPED);
            queueRepository.save(queueItem);
            videoFile.setStatus(VideoStatus.SKIPPED);
            videoFileRepository.save(videoFile);
            return false;
        }

        // 2. CHECK CONCURRENT UPLOAD
        if (duplicateDetectionService.isCurrentlyUploading(fileHash)) {
            logService.warn("QUEUE", "Video is already currently uploading: " + videoFile.getFilename(), videoFileId, fileHash);
            return false;
        }

        // 3. FILE EXISTENCE & READABILITY
        File diskFile = new File(videoFile.getFilePath());
        if (!diskFile.exists() || !diskFile.canRead()) {
            logService.error("FILE", "Video file missing on disk: " + videoFile.getFilePath(), videoFileId, fileHash);
            markJobFailure(queueItem, videoFile, "File not found or unreadable on disk: " + videoFile.getFilePath());
            return false;
        }

        // 4. METADATA RETRIEVAL
        VideoMetadata metadata = metadataRepository.findByVideoFileId(videoFileId)
                .orElseGet(() -> {
                    VideoMetadata def = new VideoMetadata();
                    def.setVideoFileId(videoFileId);
                    def.setTitle(videoFile.getFilename() + " #Shorts");
                    def.setDescription("Enjoy this viral clip! 🔥\n\n#Shorts #Viral");
                    def.setTags("shorts, viral, gaming");
                    def.setHashtags("#Shorts #Viral");
                    return metadataRepository.save(def);
                });

        // 5. UPDATE STATUS TO UPLOADING & CREATE JOB
        queueItem.setStatus(VideoStatus.UPLOADING);
        queueRepository.save(queueItem);
        videoFile.setStatus(VideoStatus.UPLOADING);
        videoFileRepository.save(videoFile);

        PublishingJob job = new PublishingJob();
        job.setQueueId(queueItem.getId());
        job.setVideoFileId(videoFileId);
        job.setFileHash(fileHash);
        job.setStatus(JobStatus.IN_PROGRESS);
        job.setStartedAt(LocalDateTime.now());
        job.setAttemptCount(1);
        PublishingJob savedJob = jobRepository.save(job);

        logService.info("UPLOAD", "Beginning upload for video: \"" + metadata.getTitle() + "\" (" + videoFile.getFilename() + ")", videoFileId, fileHash);

        // 6. EXECUTE OFFICIAL YOUTUBE API UPLOAD
        YouTubeApiService.UploadResult uploadResult = youTubeApiService.uploadVideo(diskFile, metadata, videoFileId, fileHash);

        if (uploadResult.isSuccess()) {
            // STEP 7: CONFIRMED PUBLISHED
            savedJob.setStatus(JobStatus.SUCCESS);
            savedJob.setCompletedAt(LocalDateTime.now());
            jobRepository.save(savedJob);

            // Create permanent record in published_videos
            PublishedVideo published = new PublishedVideo();
            published.setVideoFileId(videoFileId);
            published.setFileHash(fileHash);
            published.setYoutubeVideoId(uploadResult.getYoutubeVideoId());
            published.setYoutubeUrl(uploadResult.getYoutubeUrl());
            published.setTitle(metadata.getTitle());
            published.setPublishedAt(LocalDateTime.now());
            published.setPublishingStatus("PUBLISHED");
            publishedVideoRepository.save(published);

            // Update VideoFile & Queue
            videoFile.setStatus(VideoStatus.PUBLISHED);
            videoFileRepository.save(videoFile);

            queueItem.setStatus(VideoStatus.PUBLISHED);
            queueRepository.save(queueItem);

            // Create notification
            notificationRepository.save(new Notification(
                    "Video Published to YouTube! 🎉",
                    "\"" + metadata.getTitle() + "\" published successfully.\nLink: " + uploadResult.getYoutubeUrl(),
                    "SUCCESS"
            ));

            logService.info("PUBLISH", "Video successfully published: " + uploadResult.getYoutubeUrl(), videoFileId, fileHash);

            // Auto-clean raw video file from disk if enabled (conserves cloud storage while preserving database history & hash)
            if (isAutoDeleteEnabled()) {
                try {
                    if (diskFile.exists() && diskFile.delete()) {
                        logService.info("STORAGE", "Auto-cleaned raw video file after publication to conserve disk space: " + diskFile.getName(), videoFileId, fileHash);
                    }
                } catch (Exception ex) {
                    logService.warn("STORAGE", "Failed to clean up video file: " + ex.getMessage(), videoFileId, fileHash);
                }
            }

            return true;
        } else {
            // STEP 8: HANDLE FAILURE & RETRY
            logService.error("UPLOAD", "Publishing failed for \"" + videoFile.getFilename() + "\": " + uploadResult.getErrorMessage(), videoFileId, fileHash);
            markJobFailure(queueItem, videoFile, uploadResult.getErrorMessage());
            return false;
        }
    }

    private void markJobFailure(PublishingQueue queueItem, VideoFile videoFile, String errorMessage) {
        PublishingJob job = jobRepository.findTopByVideoFileIdOrderByCreatedAtDesc(videoFile.getId())
                .orElse(new PublishingJob());

        int currentAttempts = (job.getAttemptCount() != null ? job.getAttemptCount() : 0) + 1;
        job.setAttemptCount(currentAttempts);
        job.setErrorMessage(errorMessage);

        int maxRetries = job.getMaxRetries() != null ? job.getMaxRetries() : 3;
        if (currentAttempts < maxRetries) {
            job.setStatus(JobStatus.RETRY_PENDING);
            queueItem.setStatus(VideoStatus.RETRY_PENDING);
            videoFile.setStatus(VideoStatus.RETRY_PENDING);
            logService.warn("RETRY", "Upload will retry later (Attempt " + currentAttempts + "/" + maxRetries + "). Reason: " + errorMessage, videoFile.getId(), videoFile.getFileHash());
        } else {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            queueItem.setStatus(VideoStatus.FAILED);
            videoFile.setStatus(VideoStatus.FAILED);
            logService.error("FAILED", "Video reached maximum retries (" + maxRetries + "). Upload failed.", videoFile.getId(), videoFile.getFileHash());

            notificationRepository.save(new Notification(
                    "Video Upload Failed",
                    "\"" + videoFile.getFilename() + "\" failed after " + maxRetries + " attempts: " + errorMessage,
                    "ERROR"
            ));
        }

        jobRepository.save(job);
        queueRepository.save(queueItem);
        videoFileRepository.save(videoFile);
    }

    public PublishingSchedule getOrCreateSchedule() {
        return scheduleRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> {
                    PublishingSchedule s = new PublishingSchedule();
                    s.setVideosPerInterval(2);
                    s.setIntervalMinutes(60);
                    s.setStartTime("09:00");
                    s.setEndTime("23:59");
                    s.setTimezone("Asia/Kolkata");
                    s.setMaxDailyUploads(20);
                    s.setIsActive(true);
                    return scheduleRepository.save(s);
                });
    }

    private boolean isAutoDeleteEnabled() {
        String env = System.getenv("AUTO_DELETE_ON_PUBLISH");
        if ("true".equalsIgnoreCase(env)) return true;
        String prop = System.getProperty("AUTO_DELETE_ON_PUBLISH");
        if ("true".equalsIgnoreCase(prop)) return true;
        return settingRepository.findBySettingKey("AUTO_DELETE_ON_PUBLISH")
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElse(false);
    }
}
