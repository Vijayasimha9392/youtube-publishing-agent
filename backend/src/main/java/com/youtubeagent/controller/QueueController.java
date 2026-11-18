package com.youtubeagent.controller;

import com.youtubeagent.dto.VideoDetailDto;
import com.youtubeagent.model.PublishingQueue;
import com.youtubeagent.model.VideoFile;
import com.youtubeagent.model.VideoStatus;
import com.youtubeagent.repository.PublishedVideoRepository;
import com.youtubeagent.repository.PublishingQueueRepository;
import com.youtubeagent.repository.VideoFileRepository;
import com.youtubeagent.repository.VideoMetadataRepository;
import com.youtubeagent.service.YouTubePublishingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final PublishingQueueRepository queueRepository;
    private final VideoFileRepository videoFileRepository;
    private final VideoMetadataRepository metadataRepository;
    private final PublishedVideoRepository publishedVideoRepository;
    private final YouTubePublishingEngine publishingEngine;

    public QueueController(PublishingQueueRepository queueRepository,
                           VideoFileRepository videoFileRepository,
                           VideoMetadataRepository metadataRepository,
                           PublishedVideoRepository publishedVideoRepository,
                           YouTubePublishingEngine publishingEngine) {
        this.queueRepository = queueRepository;
        this.videoFileRepository = videoFileRepository;
        this.metadataRepository = metadataRepository;
        this.publishedVideoRepository = publishedVideoRepository;
        this.publishingEngine = publishingEngine;
    }

    @GetMapping
    public ResponseEntity<List<VideoDetailDto>> getQueue() {
        List<PublishingQueue> items = queueRepository.findAllByOrderByQueuePositionAsc();
        List<VideoDetailDto> list = new ArrayList<>();
        for (PublishingQueue q : items) {
            videoFileRepository.findById(q.getVideoFileId()).ifPresent(vf -> {
                VideoDetailDto dto = new VideoDetailDto();
                dto.setId(vf.getId());
                dto.setFilename(vf.getFilename());
                dto.setFilePath(vf.getFilePath());
                dto.setFileHash(vf.getFileHash());
                dto.setFileSize(vf.getFileSize());
                dto.setDuration(vf.getDuration());
                dto.setResolution(vf.getResolution());
                dto.setFormat(vf.getFormat());
                dto.setDetectedAt(vf.getDetectedAt());
                dto.setStatus(q.getStatus()); // current queue status
                dto.setQueuePosition(q.getQueuePosition());
                dto.setScheduledTime(q.getScheduledTime());
                dto.setPriority(q.getPriority());

                metadataRepository.findByVideoFileId(vf.getId()).ifPresent(m -> {
                    dto.setTitle(m.getTitle());
                    dto.setDescription(m.getDescription());
                    dto.setTags(m.getTags());
                    dto.setHashtags(m.getHashtags());
                });

                list.add(dto);
            });
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/publish-batch")
    public ResponseEntity<Map<String, Object>> publishBatch(
            @RequestParam(defaultValue = "2") int batchSize) {
        int published = publishingEngine.executePublishingBatch(batchSize, "MANUAL_DASHBOARD_TRIGGER");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "batchSize", batchSize,
                "publishedCount", published,
                "message", "Publishing batch completed. " + published + " video(s) published."
        ));
    }

    @PostMapping("/{videoFileId}/reorder")
    public ResponseEntity<Map<String, Object>> reorderQueue(
            @PathVariable Long videoFileId,
            @RequestParam int newPosition) {
        Optional<PublishingQueue> qOpt = queueRepository.findByVideoFileId(videoFileId);
        if (qOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublishingQueue item = qOpt.get();
        item.setQueuePosition(newPosition);
        item.setUpdatedAt(LocalDateTime.now());
        queueRepository.save(item);

        return ResponseEntity.ok(Map.of("success", true, "newPosition", newPosition));
    }

    @PostMapping("/{videoFileId}/retry")
    public ResponseEntity<Map<String, Object>> retryVideo(@PathVariable Long videoFileId) {
        Optional<VideoFile> vfOpt = videoFileRepository.findById(videoFileId);
        if (vfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VideoFile vf = vfOpt.get();
        vf.setStatus(VideoStatus.QUEUED);
        videoFileRepository.save(vf);

        PublishingQueue q = queueRepository.findByVideoFileId(videoFileId)
                .orElseGet(() -> {
                    PublishingQueue nq = new PublishingQueue();
                    nq.setVideoFileId(videoFileId);
                    nq.setQueuePosition(queueRepository.findMaxQueuePosition() + 1);
                    return nq;
                });
        q.setStatus(VideoStatus.QUEUED);
        q.setUpdatedAt(LocalDateTime.now());
        queueRepository.save(q);

        return ResponseEntity.ok(Map.of("success", true, "message", "Video re-queued for publishing"));
    }

    @DeleteMapping("/{videoFileId}")
    public ResponseEntity<Map<String, Object>> skipVideo(@PathVariable Long videoFileId) {
        videoFileRepository.findById(videoFileId).ifPresent(vf -> {
            vf.setStatus(VideoStatus.SKIPPED);
            videoFileRepository.save(vf);
        });

        queueRepository.findByVideoFileId(videoFileId).ifPresent(q -> {
            q.setStatus(VideoStatus.SKIPPED);
            queueRepository.save(q);
        });

        return ResponseEntity.ok(Map.of("success", true, "message", "Video skipped"));
    }
}
