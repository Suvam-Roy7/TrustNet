package com.social.SocialGraphService.DTOs;

import java.util.UUID;

import com.social.SocialGraphService.Entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateNotificationRequestDTO {

    private UUID recipientUserId;

    private UUID actorUserId;

    private NotificationType type;

    private String message;
}