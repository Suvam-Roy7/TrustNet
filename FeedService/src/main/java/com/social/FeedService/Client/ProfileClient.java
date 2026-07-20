package com.social.FeedService.Client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.social.FeedService.DTOs.FeedResponseDTO;
import com.social.FeedService.DTOs.ProfileResponseDTO;

import org.springframework.data.domain.Page;

@FeignClient(name = "ProfileService")
public interface ProfileClient {

	@GetMapping("/api/profiles/{userId}")
	ProfileResponseDTO getProfile(@PathVariable UUID userId);
}
