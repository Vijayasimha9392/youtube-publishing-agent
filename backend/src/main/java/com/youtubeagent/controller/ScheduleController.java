package com.youtubeagent.controller;

import com.youtubeagent.dto.ScheduleUpdateDto;
import com.youtubeagent.model.PublishingSchedule;
import com.youtubeagent.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final SettingsService settingsService;

    public ScheduleController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<PublishingSchedule> getSchedule() {
        return ResponseEntity.ok(settingsService.getSchedule());
    }

    @PutMapping
    public ResponseEntity<PublishingSchedule> updateSchedule(@RequestBody ScheduleUpdateDto dto) {
        return ResponseEntity.ok(settingsService.updateSchedule(dto));
    }

    @PostMapping("/toggle")
    public ResponseEntity<PublishingSchedule> toggleAutomation(@RequestParam boolean active) {
        return ResponseEntity.ok(settingsService.toggleAutomation(active));
    }
}
