package com.youtubeagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "published_videos")
public class PublishedVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_file_id", nullable = false, unique = true)
    private Long videoFileId;

    @Column(name = "file_hash", length = 64, unique = true, nullable = false)
    private String fileHash;

    @Column(name = "youtube_video_id", length = 64, unique = true, nullable = false)
    private String youtubeVideoId;

    @Column(name = "youtube_url", nullable = false)
    private String youtubeUrl;

    private String title;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt = LocalDateTime.now();

    @Column(name = "publishing_status", length = 32, nullable = false)
    private String publishingStatus = "PUBLISHED";

    public PublishedVideo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoFileId() { return videoFileId; }
    public void setVideoFileId(Long videoFileId) { this.videoFileId = videoFileId; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getPublishingStatus() { return publishingStatus; }
    public void setPublishingStatus(String publishingStatus) { this.publishingStatus = publishingStatus; }
}
