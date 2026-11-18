package com.youtubeagent.repository;

import com.youtubeagent.model.PublishedVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PublishedVideoRepository extends JpaRepository<PublishedVideo, Long> {
    Optional<PublishedVideo> findByFileHash(String fileHash);
    boolean existsByFileHash(String fileHash);
    Optional<PublishedVideo> findByYoutubeVideoId(String youtubeVideoId);
    List<PublishedVideo> findAllByOrderByPublishedAtDesc();

    @Query("SELECT COUNT(p) FROM PublishedVideo p WHERE p.publishedAt >= :startOfDay")
    long countPublishedToday(@Param("startOfDay") LocalDateTime startOfDay);
}
