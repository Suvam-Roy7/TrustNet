package com.social.NotificationService.Controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.NotificationService.DTOs.CreateNotificationRequestDTO;
import com.social.NotificationService.DTOs.NotificationResponseDTO;
import com.social.NotificationService.Service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDTO>
    createNotification(
            @RequestBody
            CreateNotificationRequestDTO request) {

        return ResponseEntity.ok(
                service.createNotification(
                        request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponseDTO>>
    getNotifications(

            @RequestParam(
                    defaultValue = "0")
            int page,

            @RequestParam(
                    defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                service.getNotifications(
                        page,
                        size));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String>
    markAsRead(
            @PathVariable UUID notificationId) {

        service.markAsRead(
                notificationId);

        return ResponseEntity.ok(
                "Notification marked as read");
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String>
    deleteNotification(
            @PathVariable UUID notificationId) {

        service.deleteNotification(
                notificationId);

        return ResponseEntity.ok(
                "Notification deleted");
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long>
    getUnreadCount() {

        return ResponseEntity.ok(
                service.getUnreadCount());
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String>
    markAllAsRead() {

        service.markAllAsRead();

        return ResponseEntity.ok(
                "All notifications marked as read");
    }
}