package com.youtubeagent.controller;

import com.youtubeagent.dto.UpdateMetadataDto;
import com.youtubeagent.dto.VideoDetailDto;
import com.youtubeagent.model.PublishedVideo;
import com.youtubeagent.model.PublishingQueue;
import com.youtubeagent.model.VideoFile;
import com.youtubeagent.model.VideoMetadata;
import com.youtubeagent.repository.PublishedVideoRepository;
import com.youtubeagent.repository.PublishingQueueRepository;
import com.youtubeagent.repository.VideoFileRepository;
import com.youtubeagent.repository.VideoMetadataRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.youtubeagent.service.FolderMonitoringService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoFileRepository videoFileRepository;
    private final VideoMetadataRepository metadataRepository;
    private final PublishingQueueRepository queueRepository;
    private final PublishedVideoRepository publishedVideoRepository;
    private final FolderMonitoringService folderMonitoringService;

    public VideoController(VideoFileRepository videoFileRepository,
                           VideoMetadataRepository metadataRepository,
                           PublishingQueueRepository queueRepository,
                           PublishedVideoRepository publishedVideoRepository,
                           FolderMonitoringService folderMonitoringService) {
        this.videoFileRepository = videoFileRepository;
        this.metadataRepository = metadataRepository;
        this.queueRepository = queueRepository;
        this.publishedVideoRepository = publishedVideoRepository;
        this.folderMonitoringService = folderMonitoringService;
    }

    @PostMapping("/upload")
    public ResponseEntity<java.util.Map<String, Object>> uploadVideos(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "No files provided"));
        }

        File targetDir = new File(folderMonitoringService.getEffectiveMonitoredFolder());
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        int savedCount = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "clip_" + System.currentTimeMillis() + ".mp4";
            }
            File dest = new File(targetDir, originalFilename);
            try {
                file.transferTo(dest);
                savedCount++;
            } catch (IOException e) {
                // Ignore single file write error
            }
        }

        int scanned = folderMonitoringService.scanFolderNow();
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "savedFiles", savedCount,
                "newVideosDetected", scanned,
                "message", "Successfully uploaded " + savedCount + " video file(s)."
        ));
    }

    @GetMapping
    public ResponseEntity<List<VideoDetailDto>> getAllVideos() {
        List<VideoFile> files = videoFileRepository.findAllByOrderByDetectedAtDesc();
        List<VideoDetailDto> list = new ArrayList<>();
        for (VideoFile f : files) {
            list.add(mapToDto(f));
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDetailDto> getVideo(@PathVariable Long id) {
        return videoFileRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/metadata")
    public ResponseEntity<VideoDetailDto> updateMetadata(@PathVariable Long id, @RequestBody UpdateMetadataDto dto) {
        Optional<VideoFile> vfOpt = videoFileRepository.findById(id);
        if (vfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VideoMetadata meta = metadataRepository.findByVideoFileId(id)
                .orElseGet(() -> {
                    VideoMetadata m = new VideoMetadata();
                    m.setVideoFileId(id);
                    return m;
                });

        if (dto.getTitle() != null) meta.setTitle(dto.getTitle());
        if (dto.getDescription() != null) meta.setDescription(dto.getDescription());
        if (dto.getTags() != null) meta.setTags(dto.getTags());
        if (dto.getHashtags() != null) meta.setHashtags(dto.getHashtags());
        if (dto.getCategoryId() != null) meta.setCategoryId(dto.getCategoryId());
        if (dto.getPrivacyStatus() != null) meta.setPrivacyStatus(dto.getPrivacyStatus());
        if (dto.getIsMadeForKids() != null) meta.setIsMadeForKids(dto.getIsMadeForKids());
        meta.setAiGenerated(false); // Manually customized
        meta.setUpdatedAt(LocalDateTime.now());
        metadataRepository.save(meta);

        return ResponseEntity.ok(mapToDto(vfOpt.get()));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        Optional<VideoFile> vfOpt = videoFileRepository.findById(id);
        if (vfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(vfOpt.get().getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource videoResource = new FileSystemResource(file);
        long contentLength = file.length();

        ResourceRegion region;
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : Math.min(start + 1048576L, contentLength - 1);
            long rangeLength = Math.min(1048576L, end - start + 1);
            region = new ResourceRegion(videoResource, start, rangeLength);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(region);
        } else {
            long rangeLength = Math.min(1048576L, contentLength);
            region = new ResourceRegion(videoResource, 0, rangeLength);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(region);
        }
    }

    private VideoDetailDto mapToDto(VideoFile file) {
        VideoDetailDto dto = new VideoDetailDto();
        dto.setId(file.getId());
        dto.setFilename(file.getFilename());
        dto.setFilePath(file.getFilePath());
        dto.setFileHash(file.getFileHash());
        dto.setFileSize(file.getFileSize());
        dto.setDuration(file.getDuration());
        dto.setResolution(file.getResolution());
        dto.setFormat(file.getFormat());
        dto.setDetectedAt(file.getDetectedAt());
        dto.setStatus(file.getStatus());

        metadataRepository.findByVideoFileId(file.getId()).ifPresent(m -> {
            dto.setTitle(m.getTitle());
            dto.setDescription(m.getDescription());
            dto.setTags(m.getTags());
            dto.setHashtags(m.getHashtags());
            dto.setCategoryId(m.getCategoryId());
            dto.setPrivacyStatus(m.getPrivacyStatus());
            dto.setIsMadeForKids(m.getIsMadeForKids());
            dto.setAiGenerated(m.getAiGenerated());
        });

        queueRepository.findByVideoFileId(file.getId()).ifPresent(q -> {
            dto.setQueuePosition(q.getQueuePosition());
            dto.setScheduledTime(q.getScheduledTime());
            dto.setPriority(q.getPriority());
        });

        publishedVideoRepository.findByFileHash(file.getFileHash()).ifPresent(p -> {
            dto.setYoutubeVideoId(p.getYoutubeVideoId());
            dto.setYoutubeUrl(p.getYoutubeUrl());
            dto.setPublishedAt(p.getPublishedAt());
        });

        return dto;
    }
}
