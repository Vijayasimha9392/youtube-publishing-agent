package com.youtubeagent.dto;

import java.time.LocalDateTime;

public class DashboardStatsDto {
    private long totalDetected;
    private long totalInQueue;
    private long totalPublished;
    private long totalPending;
    private long totalFailed;
    private long videosRemaining;
    private long todayPublishCount;
    private String nextPublishTime;
    private boolean channelConnected;
    private String channelTitle;
    private String channelHandle;
    private String channelAvatar;
    private long subscriberCount;
    private boolean monitoringActive;
    private String monitoredFolder;
    private boolean automationEnabled;
    private int videosPerInterval;
    private int intervalMinutes;
    private String timezone;

    public DashboardStatsDto() {}

    public long getTotalDetected() { return totalDetected; }
    public void setTotalDetected(long totalDetected) { this.totalDetected = totalDetected; }

    public long getTotalInQueue() { return totalInQueue; }
    public void setTotalInQueue(long totalInQueue) { this.totalInQueue = totalInQueue; }

    public long getTotalPublished() { return totalPublished; }
    public void setTotalPublished(long totalPublished) { this.totalPublished = totalPublished; }

    public long getTotalPending() { return totalPending; }
    public void setTotalPending(long totalPending) { this.totalPending = totalPending; }

    public long getTotalFailed() { return totalFailed; }
    public void setTotalFailed(long totalFailed) { this.totalFailed = totalFailed; }

    public long getVideosRemaining() { return videosRemaining; }
    public void setVideosRemaining(long videosRemaining) { this.videosRemaining = videosRemaining; }

    public long getTodayPublishCount() { return todayPublishCount; }
    public void setTodayPublishCount(long todayPublishCount) { this.todayPublishCount = todayPublishCount; }

    public String getNextPublishTime() { return nextPublishTime; }
    public void setNextPublishTime(String nextPublishTime) { this.nextPublishTime = nextPublishTime; }

    public boolean isChannelConnected() { return channelConnected; }
    public void setChannelConnected(boolean channelConnected) { this.channelConnected = channelConnected; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public String getChannelHandle() { return channelHandle; }
    public void setChannelHandle(String channelHandle) { this.channelHandle = channelHandle; }

    public String getChannelAvatar() { return channelAvatar; }
    public void setChannelAvatar(String channelAvatar) { this.channelAvatar = channelAvatar; }

    public long getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(long subscriberCount) { this.subscriberCount = subscriberCount; }

    public boolean isMonitoringActive() { return monitoringActive; }
    public void setMonitoringActive(boolean monitoringActive) { this.monitoringActive = monitoringActive; }

    public String getMonitoredFolder() { return monitoredFolder; }
    public void setMonitoredFolder(String monitoredFolder) { this.monitoredFolder = monitoredFolder; }

    public boolean isAutomationEnabled() { return automationEnabled; }
    public void setAutomationEnabled(boolean automationEnabled) { this.automationEnabled = automationEnabled; }

    public int getVideosPerInterval() { return videosPerInterval; }
    public void setVideosPerInterval(int videosPerInterval) { this.videosPerInterval = videosPerInterval; }

    public int getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
