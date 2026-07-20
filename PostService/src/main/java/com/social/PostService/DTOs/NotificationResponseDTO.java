package com.social.PostService.DTOs;

import java.util.UUID;

import com.social.PostService.Entity.NotificationType;

import lombok.Data;

@Data
public class NotificationResponseDTO {

    private UUID id;

    private UUID recipientUserId;

    private UUID actorUserId;

    private NotificationType type;

    private String message;

    private Boolean isRead;
}
