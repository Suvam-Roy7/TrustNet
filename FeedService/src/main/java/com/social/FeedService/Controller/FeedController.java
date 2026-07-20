package com.social.FeedService.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.FeedService.DTOs.FeedPostResponseDTO;
import com.social.FeedService.Service.FeedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

	private final FeedService feedService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<FeedPostResponseDTO>> getFeed(

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "20") int size) {

		return ResponseEntity.ok(feedService.getFeed(page, size));
	}
}