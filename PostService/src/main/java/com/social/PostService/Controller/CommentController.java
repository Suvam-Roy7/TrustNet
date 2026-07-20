package com.social.PostService.Controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.PostService.DTOs.CommentResponseDTO;
import com.social.PostService.DTOs.CreateCommentRequestDTO;
import com.social.PostService.Service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService service;

	@PostMapping("/{postId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CommentResponseDTO> addComment(@PathVariable UUID postId,
			@Valid @RequestBody CreateCommentRequestDTO request) {

		return ResponseEntity.ok(service.addComment(postId, request));
	}

	@GetMapping("/{postId}")
	public ResponseEntity<Page<CommentResponseDTO>> getComments(@PathVariable UUID postId,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(service.getComments(postId, page, size));
	}

	@DeleteMapping("/{commentId}")
	@PreAuthorize("hasRole('ADMIN') " + "or hasRole('MODERATOR') " + "or @commentSecurity.isOwner(#commentId)")
	public ResponseEntity<String> deleteComment(@PathVariable UUID commentId) {

		service.deleteComment(commentId);

		return ResponseEntity.ok("Comment deleted");
	}
}
