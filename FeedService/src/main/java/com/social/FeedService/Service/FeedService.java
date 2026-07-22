package com.social.FeedService.Service;

import java.util.UUID;

import com.social.FeedService.DTOs.FeedResponseDTO;

public interface FeedService {

    FeedResponseDTO getFeed(
            UUID userId,
            int page,
            int size
    );
}