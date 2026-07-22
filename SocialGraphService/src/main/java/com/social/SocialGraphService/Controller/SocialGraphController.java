package com.social.SocialGraphService.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.SocialGraphService.DTOs.FollowRequestDTO;
import com.social.SocialGraphService.DTOs.FollowRequestResponseDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.RelationshipStatusResponseDTO;
import com.social.SocialGraphService.DTOs.SendFollowRequestDTO;
import com.social.SocialGraphService.DTOs.SocialSummaryResponseDTO;
import com.social.SocialGraphService.Service.SocialGraphService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialGraphController {

	private final SocialGraphService service;

	/*
	 * Old direct-follow endpoint. Keep temporarily only if unfollow/testing still
	 * requires it. The frontend should not call this for new requests.
	 */
	@PostMapping("/follow")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FollowResponseDTO> follow(@RequestHeader("X-User-Id") UUID followerId,

			@Valid @RequestBody FollowRequestDTO request) {

		FollowResponseDTO response = service.follow(followerId, request.getFollowedUserId());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/unfollow")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> unfollow(@RequestHeader("X-User-Id") UUID followerId,

			@Valid @RequestBody FollowRequestDTO request) {

		service.unfollow(followerId, request.getFollowedUserId());

		return ResponseEntity.noContent().build();
	}

	/*
	 * Create a pending follow request.
	 */
	@PostMapping("/follow-requests")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FollowRequestResponseDTO> sendFollowRequest(@RequestHeader("X-User-Id") UUID requesterId,

			@Valid @RequestBody SendFollowRequestDTO request) {

		FollowRequestResponseDTO response = service.sendFollowRequest(requesterId, request.getReceiverId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * Get requests received by the logged-in user.
	 */
	@GetMapping("/follow-requests/incoming")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<FollowRequestResponseDTO>> getIncomingFollowRequests(
			@RequestHeader("X-User-Id") UUID receiverId) {

		return ResponseEntity.ok(service.getIncomingFollowRequests(receiverId));
	}

	@PostMapping("/follow-requests/{requestId}/accept")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FollowRequestResponseDTO> acceptFollowRequest(@RequestHeader("X-User-Id") UUID receiverId,

			@PathVariable("requestId") UUID requestId) {

		return ResponseEntity.ok(service.acceptFollowRequest(receiverId, requestId));
	}

	@PostMapping("/follow-requests/{requestId}/reject")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FollowRequestResponseDTO> rejectFollowRequest(@RequestHeader("X-User-Id") UUID receiverId,

			@PathVariable("requestId") UUID requestId) {

		return ResponseEntity.ok(service.rejectFollowRequest(receiverId, requestId));
	}

	@GetMapping("/follow-requests/{receiverId}/status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RelationshipStatusResponseDTO> getRelationshipStatus(
			@RequestHeader("X-User-Id") UUID requesterId,

			@PathVariable("receiverId") UUID receiverId) {

		return ResponseEntity.ok(service.getRelationshipStatus(requesterId, receiverId));
	}

	@GetMapping("/summary")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<SocialSummaryResponseDTO> getSocialSummary(@RequestHeader("X-User-Id") UUID userId) {

		return ResponseEntity.ok(service.getSocialSummary(userId));
	}

	@GetMapping("/following")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<UUID>> getFollowingUsers(@RequestHeader("X-User-Id") UUID userId) {

		return ResponseEntity.ok(service.getFollowing(userId));
	}
}