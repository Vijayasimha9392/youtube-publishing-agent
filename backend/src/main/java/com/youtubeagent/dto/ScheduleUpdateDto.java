package com.youtubeagent.dto;

public class ScheduleUpdateDto {
    private Integer videosPerInterval;
    private Integer intervalMinutes;
    private String startTime;
    private String endTime;
    private String timezone;
    private Integer maxDailyUploads;
    private Boolean isActive;

    public ScheduleUpdateDto() {}

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
}
