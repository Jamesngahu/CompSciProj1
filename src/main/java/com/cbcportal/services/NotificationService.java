package com.cbcportal.services;

import com.cbcportal.models.Notification;
import com.cbcportal.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Create a new notification
    public Notification sendNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    // Fetch notifications for a specific user
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    // Mark a notification as read
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (notifOpt.isPresent()) {
            Notification notification = notifOpt.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("Notification not found.");
    }
}