package com.youtubeagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_metadata")
public class VideoMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_file_id", nullable = false, unique = true)
    private Long videoFileId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String hashtags;

    @Column(name = "category_id", length = 32)
    private String categoryId = "20"; // 20 = Gaming

    @Column(name = "privacy_status", length = 32)
    private String privacyStatus = "public";

    @Column(name = "is_made_for_kids")
    private Boolean isMadeForKids = false;

    @Column(name = "ai_generated")
    private Boolean aiGenerated = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public VideoMetadata() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVideoFileId() { return videoFileId; }
    public void setVideoFileId(Long videoFileId) { this.videoFileId = videoFileId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getPrivacyStatus() { return privacyStatus; }
    public void setPrivacyStatus(String privacyStatus) { this.privacyStatus = privacyStatus; }

    public Boolean getIsMadeForKids() { return isMadeForKids; }
    public void setIsMadeForKids(Boolean isMadeForKids) { this.isMadeForKids = isMadeForKids; }

    public Boolean getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(Boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
