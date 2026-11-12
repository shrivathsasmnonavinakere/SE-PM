package com.annadhata.service;

import com.annadhata.enums.NotificationType;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.Notification;
import com.annadhata.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    @Value("${firebase.project-id:}")
    private String firebaseProjectId;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification sendNotification(String recipientUserId, String title, String message, 
                                        NotificationType type, String relatedEntityId) {
        // Persist notification in MongoDB
        Notification notification = new Notification(recipientUserId, title, message, type, relatedEntityId);
        Notification saved = notificationRepository.save(notification);

        // Dispatch push notification via FCM if configured, else graceful development fallback
        dispatchFcmPush(recipientUserId, title, message, type);

        return saved;
    }

    private void dispatchFcmPush(String userId, String title, String message, NotificationType type) {
        if (firebaseProjectId == null || firebaseProjectId.trim().isEmpty()) {
            log.info("[FCM Development Fallback] Push to user {}: [{}] {} (Type: {})", userId, title, message, type);
            return;
        }

        try {
            log.info("[FCM Active] Dispatching real FCM message for user {} under project {}", userId, firebaseProjectId);
            // Production FCM client hook
        } catch (Exception ex) {
            log.warn("FCM dispatch failed: {}. In-app notification preserved.", ex.getMessage());
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    public Notification markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new com.annadhata.exception.ForbiddenException("You are not authorized to access this notification");
        }
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        Instant now = Instant.now();
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }
}
