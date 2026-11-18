package com.youtubeagent.service;

import com.youtubeagent.model.JobStatus;
import com.youtubeagent.repository.PublishedVideoRepository;
import com.youtubeagent.repository.PublishingJobRepository;
import com.youtubeagent.repository.VideoFileRepository;
import org.springframework.stereotype.Service;

@Service
public class DuplicateDetectionService {

    private final PublishedVideoRepository publishedVideoRepository;
    private final VideoFileRepository videoFileRepository;
    private final PublishingJobRepository publishingJobRepository;
    private final AutomationLogService logService;

    public DuplicateDetectionService(PublishedVideoRepository publishedVideoRepository,
                                     VideoFileRepository videoFileRepository,
                                     PublishingJobRepository publishingJobRepository,
                                     AutomationLogService logService) {
        this.publishedVideoRepository = publishedVideoRepository;
        this.videoFileRepository = videoFileRepository;
        this.publishingJobRepository = publishingJobRepository;
        this.logService = logService;
    }

    /**
     * Checks if a video has ALREADY been published to YouTube.
     * Permanent guarantee: Uses SHA-256 file hash.
     */
    public boolean isAlreadyPublished(String fileHash) {
        if (fileHash == null || fileHash.isBlank()) {
            return false;
        }
        boolean exists = publishedVideoRepository.existsByFileHash(fileHash);
        if (exists) {
            logService.warn("DUPLICATE", "Permanent duplicate detected! Video hash " + fileHash + " was previously published. Skipping permanently.", null, fileHash);
        }
        return exists;
    }

    /**
     * Checks if a video file has already been registered in the system.
     */
    public boolean isAlreadyRegistered(String fileHash) {
        if (fileHash == null || fileHash.isBlank()) {
            return false;
        }
        return videoFileRepository.existsByFileHash(fileHash);
    }

    /**
     * Checks if a video is currently being uploaded in a concurrent job.
     */
    public boolean isCurrentlyUploading(String fileHash) {
        if (fileHash == null || fileHash.isBlank()) {
            return false;
        }
        return publishingJobRepository.existsByFileHashAndStatus(fileHash, JobStatus.IN_PROGRESS);
    }

    /**
     * Full eligibility check for publishing.
     */
    public boolean isEligibleForPublishing(String fileHash) {
        return !isAlreadyPublished(fileHash) && !isCurrentlyUploading(fileHash);
    }
}
