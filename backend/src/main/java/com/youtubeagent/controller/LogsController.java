package com.youtubeagent.controller;

import com.youtubeagent.model.AutomationLog;
import com.youtubeagent.repository.AutomationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogsController {

    private final AutomationLogRepository logRepository;

    public LogsController(AutomationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping
    public ResponseEntity<List<AutomationLog>> getRecentLogs(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "100") int limit) {

        if (category != null && !category.isBlank()) {
            Page<AutomationLog> page = logRepository.findByCategoryOrderByCreatedAtDesc(category, PageRequest.of(0, limit));
            return ResponseEntity.ok(page.getContent());
        } else {
            Page<AutomationLog> page = logRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
            return ResponseEntity.ok(page.getContent());
        }
    }
}
