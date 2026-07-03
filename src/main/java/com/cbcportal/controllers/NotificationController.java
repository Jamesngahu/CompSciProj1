package com.cbcportal.controllers;

import com.cbcportal.models.Notification;
import com.cbcportal.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Fetch all notifications for a specific user
    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    // Mark a notification as read
    @PutMapping("/read/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            return ResponseEntity.ok(notificationService.markAsRead(notificationId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}