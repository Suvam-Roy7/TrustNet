package com.social.PostService.Service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.social.PostService.DTOs.CommentResponseDTO;
import com.social.PostService.DTOs.CreateCommentRequestDTO;

public interface CommentService {

	CommentResponseDTO addComment(UUID postId, CreateCommentRequestDTO request);

	Page<CommentResponseDTO> getComments(UUID postId, int page, int size);

	void deleteComment(UUID commentId);
}
