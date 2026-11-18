package com.youtubeagent.repository;

import com.youtubeagent.model.VideoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoMetadataRepository extends JpaRepository<VideoMetadata, Long> {
    Optional<VideoMetadata> findByVideoFileId(Long videoFileId);
    void deleteByVideoFileId(Long videoFileId);
}
