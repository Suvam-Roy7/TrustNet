package com.social.FeedService.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedResponseDTO {

    private UUID postId;

    private UUID userId;

    private String username;

    private String content;

    private Long likeCount;

    private Long commentCount;

    private LocalDateTime createdAt;
}
