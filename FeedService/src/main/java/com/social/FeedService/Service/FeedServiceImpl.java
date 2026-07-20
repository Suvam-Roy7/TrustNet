package com.social.FeedService.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.social.FeedService.Client.SocialGraphClient;
import com.social.FeedService.DTOs.FeedPostResponseDTO;
import com.social.FeedService.Entity.FeedPost;
import com.social.FeedService.Repository.FeedPostRepository;
import com.social.FeedService.Service.FeedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

	private static final int MAX_PAGE_SIZE = 100;

	private final FeedPostRepository feedPostRepository;

	private final SocialGraphClient socialGraphClient;

	@Override
	public Page<FeedPostResponseDTO> getFeed(int page, int size) {

		if (page < 0) {
			throw new IllegalArgumentException("Page number cannot be negative");
		}

		if (size <= 0 || size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
		}

		UUID loggedInUserId = getLoggedInUserId();

		List<UUID> followedUserIds = socialGraphClient.getFollowing(loggedInUserId);

		List<UUID> feedAuthorIds = followedUserIds == null ? new ArrayList<>() : new ArrayList<>(followedUserIds);

		/*
		 * Include posts created by the logged-in user.
		 */
		if (!feedAuthorIds.contains(loggedInUserId)) {

			feedAuthorIds.add(loggedInUserId);
		}

		Pageable pageable = PageRequest.of(page, size);

		return feedPostRepository.findByAuthorIdInOrderByCreatedAtDesc(feedAuthorIds, pageable)
				.map(this::mapToResponse);
	}

	private FeedPostResponseDTO mapToResponse(FeedPost feedPost) {

		return FeedPostResponseDTO.builder().postId(feedPost.getPostId()).authorId(feedPost.getAuthorId())
				.content(feedPost.getContent()).createdAt(feedPost.getCreatedAt()).updatedAt(feedPost.getUpdatedAt())
				.build();
	}

	private UUID getLoggedInUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalStateException("User is not authenticated");
		}

		try {
			return UUID.fromString(authentication.getName());

		} catch (IllegalArgumentException ex) {

			throw new IllegalStateException("Invalid authenticated user ID", ex);
		}
	}
}