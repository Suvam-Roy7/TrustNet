package com.social.PostService.Service;

import java.util.List;
import java.util.UUID;

import com.social.PostService.DTOs.CommentResponseDTO;
import com.social.PostService.DTOs.CreateCommentRequestDTO;
import com.social.PostService.DTOs.CreatePostRequestDTO;
import com.social.PostService.DTOs.PostResponseDTO;
import com.social.PostService.DTOs.UpdatePostRequestDTO;

import org.springframework.data.domain.Page;

public interface PostService {

	PostResponseDTO createPost(UUID userId, CreatePostRequestDTO request);

	PostResponseDTO getPost(UUID postId);

	Page<PostResponseDTO> getUserPosts(UUID userId, int page, int size);

	void deletePost(UUID postId);

	PostResponseDTO updatePost(UUID postId, UpdatePostRequestDTO request);

	void likePost(UUID postId, UUID userId);

	void unlikePost(UUID postId, UUID userId);

	long getLikeCount(UUID postId);

	List<PostResponseDTO> getAllUserPosts(UUID userId);

}
