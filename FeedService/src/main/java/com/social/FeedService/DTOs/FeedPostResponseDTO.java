package com.social.FeedService.DTOs;

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
public class FeedPostResponseDTO {

    private UUID postId;

    private UUID authorId;

    private String content;

    private Instant createdAt;
    
    private Instant updatedAt;
}