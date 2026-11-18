package com.youtubeagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_logs")
public class AutomationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 16, nullable = false)
    private String level = "INFO";

    @Column(length = 32, nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "video_file_id")
    private Long videoFileId;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AutomationLog() {}

    public AutomationLog(String level, String category, String message, Long videoFileId, String fileHash) {
        this.level = level;
        this.category = category;
        this.message = message;
        this.videoFileId = videoFileId;
        this.fileHash = fileHash;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getVideoFileId() { return videoFileId; }
    public void setVideoFileId(Long videoFileId) { this.videoFileId = videoFileId; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
