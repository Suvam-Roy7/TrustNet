package com.social.FeedService.DTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponseDTO {

    private List<FeedPostResponseDTO> content;

    private int page;

    private int size;

    private boolean hasMore;

    private int followingPostCount;

    private int ownPostCount;

    private int suggestedPostCount;
}