package com.youtubeagent.service;

import com.youtubeagent.model.AutomationLog;
import com.youtubeagent.repository.AutomationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AutomationLogService {

    private static final Logger log = LoggerFactory.getLogger(AutomationLogService.class);
    private final AutomationLogRepository logRepository;

    public AutomationLogService(AutomationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void info(String category, String message) {
        info(category, message, null, null);
    }

    public void info(String category, String message, Long videoFileId, String fileHash) {
        log.info("[{}] {}", category, message);
        saveLog("INFO", category, message, videoFileId, fileHash);
    }

    public void warn(String category, String message) {
        warn(category, message, null, null);
    }

    public void warn(String category, String message, Long videoFileId, String fileHash) {
        log.warn("[{}] {}", category, message);
        saveLog("WARN", category, message, videoFileId, fileHash);
    }

    public void error(String category, String message) {
        error(category, message, null, null);
    }

    public void error(String category, String message, Long videoFileId, String fileHash) {
        log.error("[{}] {}", category, message);
        saveLog("ERROR", category, message, videoFileId, fileHash);
    }

    private void saveLog(String level, String category, String message, Long videoFileId, String fileHash) {
        try {
            AutomationLog entry = new AutomationLog(level, category, message, videoFileId, fileHash);
            logRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to save automation log to database: {}", e.getMessage());
        }
    }
}
