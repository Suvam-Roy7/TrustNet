package com.social.SocialGraphService.DTOs;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponseDTO {

    private UUID id;

    private UUID followerId;

    private UUID followedUserId;

    private Instant createdAt;
}