package com.youtubeagent.repository;

import com.youtubeagent.model.PublishingQueue;
import com.youtubeagent.model.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishingQueueRepository extends JpaRepository<PublishingQueue, Long> {
    Optional<PublishingQueue> findByVideoFileId(Long videoFileId);
    List<PublishingQueue> findByStatusOrderByQueuePositionAsc(VideoStatus status);
    List<PublishingQueue> findAllByOrderByQueuePositionAsc();
    
    @Query("SELECT COALESCE(MAX(q.queuePosition), 0) FROM PublishingQueue q")
    Integer findMaxQueuePosition();

    long countByStatus(VideoStatus status);
}
