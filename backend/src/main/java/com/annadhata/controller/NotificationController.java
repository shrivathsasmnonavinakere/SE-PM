package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.model.Notification;
import com.annadhata.model.User;
import com.annadhata.service.NotificationService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Endpoints for managing real-time and in-app notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get notification history for authenticated user")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        User user = SecurityUtils.getCurrentUser();
        List<Notification> list = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        User user = SecurityUtils.getCurrentUser();
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String id) {
        User user = SecurityUtils.getCurrentUser();
        Notification updated = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User user = SecurityUtils.getCurrentUser();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
