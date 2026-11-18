package com.youtubeagent.service;

import com.youtubeagent.dto.ScheduleUpdateDto;
import com.youtubeagent.dto.SettingsDto;
import com.youtubeagent.model.ApiCredential;
import com.youtubeagent.model.AutomationSetting;
import com.youtubeagent.model.PublishingSchedule;
import com.youtubeagent.repository.ApiCredentialRepository;
import com.youtubeagent.repository.AutomationSettingRepository;
import com.youtubeagent.repository.PublishingScheduleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SettingsService {

    private final AutomationSettingRepository settingRepository;
    private final ApiCredentialRepository credentialRepository;
    private final PublishingScheduleRepository scheduleRepository;
    private final FolderMonitoringService folderMonitoringService;
    private final AutomationLogService logService;

    @Value("${agent.youtube.redirect-uri:http://localhost:8080/api/youtube/oauth2/callback}")
    private String defaultRedirectUri;

    public SettingsService(AutomationSettingRepository settingRepository,
                           ApiCredentialRepository credentialRepository,
                           PublishingScheduleRepository scheduleRepository,
                           FolderMonitoringService folderMonitoringService,
                           AutomationLogService logService) {
        this.settingRepository = settingRepository;
        this.credentialRepository = credentialRepository;
        this.scheduleRepository = scheduleRepository;
        this.folderMonitoringService = folderMonitoringService;
        this.logService = logService;
    }

    public SettingsDto getSettings() {
        SettingsDto dto = new SettingsDto();
        dto.setMonitoredFolder(folderMonitoringService.getEffectiveMonitoredFolder());

        credentialRepository.findByProvider("GEMINI_API")
                .ifPresent(cred -> dto.setGeminiApiKey(cred.getApiKey()));

        credentialRepository.findByProvider("YOUTUBE_OAUTH")
                .ifPresent(cred -> {
                    dto.setYoutubeClientId(cred.getClientId());
                    dto.setYoutubeClientSecret(cred.getClientSecret());
                    dto.setYoutubeRedirectUri(cred.getRedirectUri() != null ? cred.getRedirectUri() : defaultRedirectUri);
                });

        if (dto.getYoutubeRedirectUri() == null || dto.getYoutubeRedirectUri().isBlank()) {
            dto.setYoutubeRedirectUri(defaultRedirectUri);
        }

        settingRepository.findBySettingKey("DEFAULT_DESCRIPTION_TEMPLATE")
                .ifPresent(s -> dto.setDefaultDescriptionTemplate(s.getSettingValue()));

        settingRepository.findBySettingKey("DEFAULT_TAGS")
                .ifPresent(s -> dto.setDefaultTags(s.getSettingValue()));

        dto.setSimulateUpload("true".equalsIgnoreCase(System.getProperty("SIMULATE_YOUTUBE_UPLOAD", "false")));

        boolean autoDel = settingRepository.findBySettingKey("AUTO_DELETE_ON_PUBLISH")
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElseGet(() -> "true".equalsIgnoreCase(System.getenv("AUTO_DELETE_ON_PUBLISH")));
        dto.setAutoDeleteOnPublish(autoDel);

        return dto;
    }

    public void updateSettings(SettingsDto dto) {
        if (dto.getMonitoredFolder() != null && !dto.getMonitoredFolder().isBlank()) {
            folderMonitoringService.updateMonitoredFolder(dto.getMonitoredFolder());
        }

        if (dto.getGeminiApiKey() != null) {
            ApiCredential gemini = credentialRepository.findByProvider("GEMINI_API")
                    .orElse(new ApiCredential());
            gemini.setProvider("GEMINI_API");
            gemini.setApiKey(dto.getGeminiApiKey());
            gemini.setIsConfigured(!dto.getGeminiApiKey().isBlank());
            gemini.setUpdatedAt(LocalDateTime.now());
            credentialRepository.save(gemini);
        }

        if (dto.getYoutubeClientId() != null || dto.getYoutubeClientSecret() != null) {
            ApiCredential yt = credentialRepository.findByProvider("YOUTUBE_OAUTH")
                    .orElse(new ApiCredential());
            yt.setProvider("YOUTUBE_OAUTH");
            if (dto.getYoutubeClientId() != null) yt.setClientId(dto.getYoutubeClientId());
            if (dto.getYoutubeClientSecret() != null) yt.setClientSecret(dto.getYoutubeClientSecret());
            if (dto.getYoutubeRedirectUri() != null) yt.setRedirectUri(dto.getYoutubeRedirectUri());
            yt.setIsConfigured(yt.getClientId() != null && !yt.getClientId().isBlank());
            yt.setUpdatedAt(LocalDateTime.now());
            credentialRepository.save(yt);
        }

        if (dto.getDefaultDescriptionTemplate() != null) {
            saveSetting("DEFAULT_DESCRIPTION_TEMPLATE", dto.getDefaultDescriptionTemplate(), "Default YouTube description template");
        }

        if (dto.getDefaultTags() != null) {
            saveSetting("DEFAULT_TAGS", dto.getDefaultTags(), "Default video tags");
        }

        saveSetting("AUTO_DELETE_ON_PUBLISH", String.valueOf(dto.isAutoDeleteOnPublish()), "Auto-delete raw video file after publication to conserve disk space");

        System.setProperty("SIMULATE_YOUTUBE_UPLOAD", String.valueOf(dto.isSimulateUpload()));
        logService.info("SETTINGS", "System settings updated successfully.");
    }

    public PublishingSchedule getSchedule() {
        return scheduleRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> {
                    PublishingSchedule s = new PublishingSchedule();
                    s.setVideosPerInterval(2);
                    s.setIntervalMinutes(60);
                    s.setStartTime("09:00");
                    s.setEndTime("23:59");
                    s.setTimezone("Asia/Kolkata");
                    s.setMaxDailyUploads(20);
                    s.setIsActive(true);
                    return scheduleRepository.save(s);
                });
    }

    public PublishingSchedule updateSchedule(ScheduleUpdateDto dto) {
        PublishingSchedule s = getSchedule();
        if (dto.getVideosPerInterval() != null) s.setVideosPerInterval(dto.getVideosPerInterval());
        if (dto.getIntervalMinutes() != null) s.setIntervalMinutes(dto.getIntervalMinutes());
        if (dto.getStartTime() != null) s.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) s.setEndTime(dto.getEndTime());
        if (dto.getTimezone() != null) s.setTimezone(dto.getTimezone());
        if (dto.getMaxDailyUploads() != null) s.setMaxDailyUploads(dto.getMaxDailyUploads());
        if (dto.getIsActive() != null) s.setIsActive(dto.getIsActive());
        s.setUpdatedAt(LocalDateTime.now());
        PublishingSchedule saved = scheduleRepository.save(s);
        logService.info("SETTINGS", "Publishing schedule updated: " + saved.getVideosPerInterval() + " videos every " + saved.getIntervalMinutes() + " mins. Active: " + saved.getIsActive());
        return saved;
    }

    public PublishingSchedule toggleAutomation(boolean active) {
        PublishingSchedule s = getSchedule();
        s.setIsActive(active);
        s.setUpdatedAt(LocalDateTime.now());
        PublishingSchedule saved = scheduleRepository.save(s);
        logService.info("SETTINGS", "Automation status set to: " + (active ? "ENABLED" : "PAUSED"));
        return saved;
    }

    private void saveSetting(String key, String val, String desc) {
        AutomationSetting s = settingRepository.findBySettingKey(key)
                .orElse(new AutomationSetting(key, val, desc));
        s.setSettingValue(val);
        s.setUpdatedAt(LocalDateTime.now());
        settingRepository.save(s);
    }
}
