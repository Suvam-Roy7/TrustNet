package com.social.PostService.DTOs;

import java.util.UUID;

import com.social.PostService.Entity.NotificationType;

import lombok.Data;

@Data
public class CreateNotificationRequestDTO {

    private UUID recipientUserId;

    private UUID actorUserId;

    private NotificationType type;

    private String message;
}
