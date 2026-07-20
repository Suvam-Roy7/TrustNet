package com.social.SocialGraphService.DTOs;

import java.util.UUID;

import com.social.SocialGraphService.Entity.NotificationType;

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
