package com.youtubeagent.controller;

import com.youtubeagent.dto.SettingsDto;
import com.youtubeagent.service.FolderMonitoringService;
import com.youtubeagent.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final FolderMonitoringService folderMonitoringService;

    public SettingsController(SettingsService settingsService,
                              FolderMonitoringService folderMonitoringService) {
        this.settingsService = settingsService;
        this.folderMonitoringService = folderMonitoringService;
    }

    @GetMapping
    public ResponseEntity<SettingsDto> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody SettingsDto dto) {
        settingsService.updateSettings(dto);
        return ResponseEntity.ok(Map.of("success", true, "message", "Settings updated successfully"));
    }

    @PostMapping("/scan-folder")
    public ResponseEntity<Map<String, Object>> triggerScan() {
        int newVideos = folderMonitoringService.scanFolderNow();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "newVideosDetected", newVideos,
                "message", "Folder scan completed. " + newVideos + " new video(s) registered."
        ));
    }
}
