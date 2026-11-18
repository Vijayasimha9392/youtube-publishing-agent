package com.youtubeagent.service;

import com.youtubeagent.dto.DashboardStatsDto;
import com.youtubeagent.model.PublishingSchedule;
import com.youtubeagent.model.VideoStatus;
import com.youtubeagent.model.YouTubeAccount;
import com.youtubeagent.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class DashboardService {

    private final VideoFileRepository videoFileRepository;
    private final PublishingQueueRepository queueRepository;
    private final PublishedVideoRepository publishedVideoRepository;
    private final PublishingJobRepository jobRepository;
    private final YouTubeAccountRepository accountRepository;
    private final PublishingScheduleRepository scheduleRepository;
    private final FolderMonitoringService folderMonitoringService;

    public DashboardService(VideoFileRepository videoFileRepository,
                            PublishingQueueRepository queueRepository,
                            PublishedVideoRepository publishedVideoRepository,
                            PublishingJobRepository jobRepository,
                            YouTubeAccountRepository accountRepository,
                            PublishingScheduleRepository scheduleRepository,
                            FolderMonitoringService folderMonitoringService) {
        this.videoFileRepository = videoFileRepository;
        this.queueRepository = queueRepository;
        this.publishedVideoRepository = publishedVideoRepository;
        this.jobRepository = jobRepository;
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.folderMonitoringService = folderMonitoringService;
    }

    public DashboardStatsDto getStats() {
        DashboardStatsDto dto = new DashboardStatsDto();

        long totalDetected = videoFileRepository.count();
        long inQueue = queueRepository.countByStatus(VideoStatus.QUEUED);
        long published = publishedVideoRepository.count();
        long failed = videoFileRepository.countByStatus(VideoStatus.FAILED);
        long pending = inQueue + queueRepository.countByStatus(VideoStatus.RETRY_PENDING);
        long remaining = Math.max(0, totalDetected - published);

        dto.setTotalDetected(totalDetected);
        dto.setTotalInQueue(inQueue);
        dto.setTotalPublished(published);
        dto.setTotalPending(pending);
        dto.setTotalFailed(failed);
        dto.setVideosRemaining(remaining);

        // Today's publishing count
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDateTime startOfDay = LocalDate.now(zone).atStartOfDay();
        dto.setTodayPublishCount(publishedVideoRepository.countPublishedToday(startOfDay));

        // Schedule & Next Publishing Time
        PublishingSchedule schedule = scheduleRepository.findTopByOrderByIdAsc()
                .orElse(new PublishingSchedule());
        dto.setAutomationEnabled(Boolean.TRUE.equals(schedule.getIsActive()));
        dto.setVideosPerInterval(schedule.getVideosPerInterval() != null ? schedule.getVideosPerInterval() : 2);
        dto.setIntervalMinutes(schedule.getIntervalMinutes() != null ? schedule.getIntervalMinutes() : 60);
        dto.setTimezone(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Kolkata");

        LocalDateTime now = LocalDateTime.now(zone);
        int interval = dto.getIntervalMinutes();
        int minutesUntilNext = interval - (now.getMinute() % interval);
        LocalDateTime nextSlot = now.plusMinutes(minutesUntilNext).withSecond(0).withNano(0);
        dto.setNextPublishTime(nextSlot.format(DateTimeFormatter.ofPattern("hh:mm a")) + " (IST)");

        // YouTube Account Status
        Optional<YouTubeAccount> activeAcc = accountRepository.findTopByIsActiveTrueOrderByIdDesc();
        if (activeAcc.isPresent()) {
            YouTubeAccount acc = activeAcc.get();
            dto.setChannelConnected(true);
            dto.setChannelTitle(acc.getChannelTitle());
            dto.setChannelHandle(acc.getChannelHandle());
            dto.setChannelAvatar(acc.getAvatarUrl());
            dto.setSubscriberCount(acc.getSubscriberCount() != null ? acc.getSubscriberCount() : 0L);
        } else {
            dto.setChannelConnected(false);
            dto.setChannelTitle("Not Connected");
            dto.setChannelHandle("@none");
        }

        // Folder Monitoring Status
        dto.setMonitoringActive(true);
        dto.setMonitoredFolder(folderMonitoringService.getEffectiveMonitoredFolder());

        return dto;
    }
}
