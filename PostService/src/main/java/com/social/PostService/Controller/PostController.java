package com.social.PostService.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.PostService.DTOs.*;
import com.social.PostService.Service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService service;

	@PostMapping
	public ResponseEntity<PostResponseDTO> createPost(@RequestHeader("X-User-Id")
    String userId, @Valid @RequestBody CreatePostRequestDTO request) {

		return ResponseEntity.ok(
	            service.createPost(
	                    UUID.fromString(userId),
	                    request));
	}

	@GetMapping("/{postId}")
	public ResponseEntity<PostResponseDTO> getPost(@PathVariable UUID postId) {

		return ResponseEntity.ok(service.getPost(postId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Page<PostResponseDTO>> getUserPosts(@PathVariable UUID userId,
			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(service.getUserPosts(userId, page, size));
	}

	@DeleteMapping("/{postId}")
	@PreAuthorize(
	        "hasRole('ADMIN') "
	      + "or hasRole('MODERATOR') "
	      + "or @postSecurity.isOwner(#postId)")
	public ResponseEntity<String> deletePost(
	        @PathVariable UUID postId) {

	    service.deletePost(postId);

	    return ResponseEntity.ok(
	            "Post Deleted Successfully");
	}

	@PutMapping("/{postId}")
	@PreAuthorize(
	        "hasRole('ADMIN') "
	      + "or hasRole('MODERATOR') "
	      + "or @postSecurity.isOwner(#postId)")
	public ResponseEntity<PostResponseDTO> updatePost(
	        @PathVariable UUID postId,
	        @Valid @RequestBody UpdatePostRequestDTO request) {

	    return ResponseEntity.ok(
	            service.updatePost(
	                    postId,
	                    request));
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<String> likePost(@PathVariable UUID postId, @RequestHeader("X-User-Id")String userId) {

		service.likePost(postId, UUID.fromString(userId));

		return ResponseEntity.ok("Post liked");
	}

	@DeleteMapping("/{postId}/like")
	public ResponseEntity<String> unlikePost(@PathVariable UUID postId, @RequestHeader("X-User-Id")
    String userId) {

		service.unlikePost(postId, UUID.fromString(userId));

		return ResponseEntity.ok("Post unliked");
	}

	@GetMapping("/{postId}/likes/count")
	public ResponseEntity<Long> getLikeCount(@PathVariable UUID postId) {

		return ResponseEntity.ok(service.getLikeCount(postId));
	}

	@GetMapping("/user/{userId}/all")
	public ResponseEntity<List<PostResponseDTO>> getAllUserPosts(@PathVariable UUID userId) {

		return ResponseEntity.ok(service.getAllUserPosts(userId));
	}
}
