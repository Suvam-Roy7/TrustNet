package com.social.FeedService.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class PostResponseDTO {

    private UUID id;

    private UUID userId;

    private String content;

    private Long likeCount;

    private Long commentCount;

    private List<String> hashtags;

    private List<String> mentions;

    private List<MediaResponseDTO> attachments;

    private LocalDateTime createdAt;
}