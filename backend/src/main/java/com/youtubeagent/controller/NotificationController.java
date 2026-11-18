package com.youtubeagent.controller;

import com.youtubeagent.model.Notification;
import com.youtubeagent.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications() {
        List<Notification> list = notificationRepository.findTop20ByOrderByCreatedAtDesc();
        long unread = notificationRepository.countByIsReadFalse();
        return ResponseEntity.ok(Map.of(
                "notifications", list,
                "unreadCount", unread
        ));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        List<Notification> list = notificationRepository.findAll();
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
