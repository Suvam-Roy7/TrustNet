package com.social.NotificationService.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import com.social.NotificationService.Entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponseDTO {

    private UUID id;

    private UUID recipientUserId;

    private UUID actorUserId;

    private NotificationType type;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
}