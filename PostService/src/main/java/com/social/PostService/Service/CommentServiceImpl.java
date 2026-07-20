package com.social.PostService.Service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.social.PostService.Client.AuthClient;
import com.social.PostService.Client.NotificationClient;
import com.social.PostService.Client.ProfileClient;
import com.social.PostService.DTOs.CommentResponseDTO;
import com.social.PostService.DTOs.ProfileResponseDTO;
import com.social.PostService.DTOs.CreateCommentRequestDTO;
import com.social.PostService.DTOs.CreateNotificationRequestDTO;
import com.social.PostService.DTOs.UserResponseDTO;
import com.social.PostService.Entity.Comment;
import com.social.PostService.Entity.NotificationType;
import com.social.PostService.Entity.Post;
import com.social.PostService.Exception.PostNotFoundException;
import com.social.PostService.Exception.UnauthorizedActionException;
import com.social.PostService.Repository.CommentRepository;
import com.social.PostService.Repository.PostRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import java.time.Instant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.social.PostService.Event.CommentCreatedDomainEvent;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;

	private final PostRepository postRepository;

	private final AuthClient authClient;

	private final NotificationClient notificationClient;

	private final ProfileClient profileClient;

	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional
	public CommentResponseDTO addComment(UUID postId, CreateCommentRequestDTO request) {

		UUID userId = getLoggedInUserId();

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {
			throw new RuntimeException("User account is inactive");
		}

		Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));

		Comment comment = Comment.builder().postId(postId).userId(userId).content(request.getContent()).build();

		Comment savedComment = commentRepository.save(comment);

		// Do not notify when the post owner comments on their own post.
		if (!post.getUserId().equals(userId)) {

			ProfileResponseDTO actorProfile = profileClient.getProfile(userId);

			eventPublisher.publishEvent(new CommentCreatedDomainEvent(UUID.randomUUID(), savedComment.getId(),
					post.getId(), userId, post.getUserId(), actorProfile.getUsername(), Instant.now()));
		}

		return mapToDTO(savedComment);
	}

	@Override
	public Page<CommentResponseDTO> getComments(UUID postId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable).map(this::mapToDTO);
	}

	@Override
	public void deleteComment(UUID commentId) {

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));

		commentRepository.delete(comment);
	}

	private CommentResponseDTO mapToDTO(Comment comment) {

		return CommentResponseDTO.builder().id(comment.getId()).postId(comment.getPostId()).userId(comment.getUserId())
				.content(comment.getContent()).createdAt(comment.getCreatedAt()).build();
	}

	private UUID getLoggedInUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {

			throw new UnauthorizedActionException("User is not authenticated");
		}

		try {
			return UUID.fromString(authentication.getName());

		} catch (IllegalArgumentException ex) {

			throw new UnauthorizedActionException("Invalid authenticated user ID");
		}
	}
}
