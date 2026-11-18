package com.youtubeagent.repository;

import com.youtubeagent.model.JobStatus;
import com.youtubeagent.model.PublishingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishingJobRepository extends JpaRepository<PublishingJob, Long> {
    List<PublishingJob> findByStatus(JobStatus status);
    List<PublishingJob> findByVideoFileId(Long videoFileId);
    Optional<PublishingJob> findTopByVideoFileIdOrderByCreatedAtDesc(Long videoFileId);
    boolean existsByFileHashAndStatus(String fileHash, JobStatus status);
    long countByStatus(JobStatus status);
}
