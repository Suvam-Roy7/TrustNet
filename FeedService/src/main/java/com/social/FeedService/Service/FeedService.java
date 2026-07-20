package com.social.FeedService.Service;

import java.util.UUID;

import com.social.FeedService.DTOs.FeedPostResponseDTO;
import org.springframework.data.domain.Page;


public interface FeedService {

	Page<FeedPostResponseDTO> getFeed(
            int page,
            int size);

}
