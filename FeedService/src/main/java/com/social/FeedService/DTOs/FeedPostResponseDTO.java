package com.social.FeedService.DTOs;

import java.util.UUID;

import com.social.FeedService.Entity.FeedSourceType;
import com.social.FeedService.Entity.SuggestionReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostResponseDTO {

    private UUID postId;

    private UUID authorUserId;

    private String authorUsername;

    private String authorProfilePictureUrl;

    private String content;

    private long likeCount;

    private long commentCount;

    private boolean likedByCurrentUser;

    private Instant  createdAt;

    private FeedSourceType sourceType;

    private SuggestionReason suggestionReason;

    private String suggestionReasonText;
}