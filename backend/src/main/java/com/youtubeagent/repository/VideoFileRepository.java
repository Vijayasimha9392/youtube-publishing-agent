package com.youtubeagent.repository;

import com.youtubeagent.model.VideoFile;
import com.youtubeagent.model.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoFileRepository extends JpaRepository<VideoFile, Long> {
    Optional<VideoFile> findByFileHash(String fileHash);
    Optional<VideoFile> findByFilePath(String filePath);
    boolean existsByFileHash(String fileHash);
    List<VideoFile> findByStatus(VideoStatus status);
    List<VideoFile> findAllByOrderByDetectedAtDesc();
    long countByStatus(VideoStatus status);
}
