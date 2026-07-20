package com.social.FeedService.Client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.social.FeedService.DTOs.FeedResponseDTO;
import com.social.FeedService.DTOs.PostResponseDTO;

import org.springframework.data.domain.Page;

@FeignClient(name = "post-service")
public interface PostClient {

	@GetMapping("/api/posts/user/{userId}/all")
	List<PostResponseDTO> getAllUserPosts(@PathVariable UUID userId);
}
