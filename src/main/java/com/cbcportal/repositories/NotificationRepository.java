package com.cbcportal.repositories;

import com.cbcportal.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Get notifications for a specific user, ordered by newest first
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}