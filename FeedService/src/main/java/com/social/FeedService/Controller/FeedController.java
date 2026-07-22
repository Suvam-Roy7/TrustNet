package com.social.FeedService.Controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.FeedService.DTOs.FeedResponseDTO;
import com.social.FeedService.Service.FeedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

	private final FeedService feedService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FeedResponseDTO> getFeed(

			@RequestHeader("X-User-Id") UUID userId,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size) {

		FeedResponseDTO response = feedService.getFeed(userId, page, size);

		return ResponseEntity.ok(response);
	}
}