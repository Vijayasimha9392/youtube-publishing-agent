package com.youtubeagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publishing_schedules")
public class PublishingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "videos_per_interval")
    private Integer videosPerInterval = 2;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes = 60;

    @Column(name = "start_time", length = 8)
    private String startTime = "09:00";

    @Column(name = "end_time", length = 8)
    private String endTime = "23:59";

    @Column(length = 64)
    private String timezone = "Asia/Kolkata";

    @Column(name = "max_daily_uploads")
    private Integer maxDailyUploads = 20;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PublishingSchedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVideosPerInterval() { return videosPerInterval; }
    public void setVideosPerInterval(Integer videosPerInterval) { this.videosPerInterval = videosPerInterval; }

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Integer getMaxDailyUploads() { return maxDailyUploads; }
    public void setMaxDailyUploads(Integer maxDailyUploads) { this.maxDailyUploads = maxDailyUploads; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
