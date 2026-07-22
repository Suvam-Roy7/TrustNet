package com.social.SocialGraphService.DTOs;

import java.time.Instant;
import java.util.UUID;

import com.social.SocialGraphService.Entity.FollowRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestResponseDTO {

    private UUID requestId;

    private UUID requesterId;

    private UUID receiverId;

    private FollowRequestStatus status;

    private Instant createdAt;

    private Instant respondedAt;
}