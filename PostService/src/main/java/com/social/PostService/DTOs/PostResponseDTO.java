package com.social.PostService.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponseDTO {

    private UUID id;

    private UUID userId;

    private String content;

    private LocalDateTime createdAt;
    
    private long likeCount;

    private long commentCount;
    
    private List<String> hashtags;
    
    private List<String> mentions;
    
    private List<MediaResponseDTO> attachments;
}
