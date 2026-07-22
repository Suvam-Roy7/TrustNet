package com.social.FeedService.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.FeedService.DTOs.FeedPostResponseDTO;
import com.social.FeedService.DTOs.FeedResponseDTO;
import com.social.FeedService.Entity.FeedPost;
import com.social.FeedService.Entity.FeedSourceType;
import com.social.FeedService.Entity.SuggestionReason;
import com.social.FeedService.Repository.FeedFollowRepository;
import com.social.FeedService.Repository.FeedPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

	private static final int MAX_PAGE_SIZE = 50;

	private final FeedPostRepository feedPostRepository;

	private final FeedFollowRepository feedFollowRepository;

	@Override
	public FeedResponseDTO getFeed(UUID userId, int page, int size) {

		validateFeedRequest(userId, page, size);

		FeedQuota quota = calculateQuota(size);

		/*
		 * Users directly followed by the current user.
		 */
		Set<UUID> followedUserIds = new HashSet<>(feedFollowRepository.findFollowedUserIdsByFollowerId(userId));

		Page<FeedPost> followingPostsPage = loadFollowingPosts(followedUserIds, page, quota.followingQuota());

		Page<FeedPost> ownPostsPage = loadOwnPosts(userId, page, quota.ownQuota());

		/*
		 * Suggested authors must not include:
		 *
		 * 1. Current user 2. Already-followed users
		 */
		Set<UUID> excludedSuggestedAuthors = new HashSet<>(followedUserIds);

		excludedSuggestedAuthors.add(userId);

		List<FeedPost> suggestedPosts = loadSecondDegreeSuggestions(userId, excludedSuggestedAuthors, page,
				quota.suggestedQuota());

		/*
		 * LinkedHashMap removes duplicates using postId.
		 */
		Map<UUID, FeedPostResponseDTO> uniqueFeedPosts = new LinkedHashMap<>();

		addPosts(uniqueFeedPosts, followingPostsPage.getContent(), FeedSourceType.FOLLOWING, null);

		addPosts(uniqueFeedPosts, ownPostsPage.getContent(), FeedSourceType.OWN, null);

		addPosts(uniqueFeedPosts, suggestedPosts, FeedSourceType.SUGGESTED,
				SuggestionReason.CONNECTION_FOLLOWS_THIS_PERSON);

		/*
		 * Sort the mixed feed by newest post first.
		 */
		List<FeedPostResponseDTO> content = uniqueFeedPosts.values().stream().sorted(Comparator
				.comparing(FeedPostResponseDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(size).toList();

		int followingPostCount = countSourceType(content, FeedSourceType.FOLLOWING);

		int ownPostCount = countSourceType(content, FeedSourceType.OWN);

		int suggestedPostCount = countSourceType(content, FeedSourceType.SUGGESTED);

		boolean hasMore = followingPostsPage.hasNext() || ownPostsPage.hasNext()
				|| (quota.suggestedQuota() > 0 && suggestedPosts.size() >= quota.suggestedQuota());

		return FeedResponseDTO.builder().content(content).page(page).size(size).hasMore(hasMore)
				.followingPostCount(followingPostCount).ownPostCount(ownPostCount)
				.suggestedPostCount(suggestedPostCount).build();
	}

	private Page<FeedPost> loadFollowingPosts(Set<UUID> followedUserIds, int page, int limit) {

		Pageable pageable = PageRequest.of(page, Math.max(limit, 1));

		if (followedUserIds.isEmpty() || limit <= 0) {

			return Page.empty(pageable);
		}

		return feedPostRepository.findByAuthorIdInOrderByCreatedAtDesc(followedUserIds, pageable);
	}

	private Page<FeedPost> loadOwnPosts(UUID userId, int page, int limit) {

		Pageable pageable = PageRequest.of(page, Math.max(limit, 1));

		if (limit <= 0) {
			return Page.empty(pageable);
		}

		return feedPostRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
	}

	private List<FeedPost> loadSecondDegreeSuggestions(UUID userId, Set<UUID> excludedAuthorIds, int page, int limit) {

		if (limit <= 0) {
			return List.of();
		}

		Pageable pageable = PageRequest.of(page, limit);

		return feedPostRepository.findSecondDegreeSuggestedPosts(userId, excludedAuthorIds, pageable);
	}

	private void addPosts(Map<UUID, FeedPostResponseDTO> destination, List<FeedPost> posts, FeedSourceType sourceType,
			SuggestionReason suggestionReason) {

		if (posts == null || posts.isEmpty()) {
			return;
		}

		for (FeedPost post : posts) {

			if (post == null || post.getPostId() == null) {

				continue;
			}

			FeedPostResponseDTO response = mapToResponse(post, sourceType, suggestionReason);

			destination.putIfAbsent(post.getPostId(), response);
		}
	}

	private FeedPostResponseDTO mapToResponse(FeedPost post, FeedSourceType sourceType,
			SuggestionReason suggestionReason) {

		return FeedPostResponseDTO.builder().postId(post.getPostId())

				.authorUserId(post.getAuthorId())

				.authorUsername(normalizeUsername(post.getAuthorUsername()))

				/*
				 * Add this after profile pictures are copied into the FeedService read model.
				 */
				.authorProfilePictureUrl(null)

				.content(post.getContent())

				.likeCount(safeLong(post.getLikeCount()))

				.commentCount(safeLong(post.getCommentCount()))

				/*
				 * Add this after FeedService maintains the current user's like read model.
				 */
				.likedByCurrentUser(false)

				.createdAt(post.getCreatedAt()).sourceType(sourceType)

				.suggestionReason(suggestionReason)

				.suggestionReasonText(getSuggestionReasonText(suggestionReason))

				.build();
	}

	private int countSourceType(List<FeedPostResponseDTO> posts, FeedSourceType sourceType) {

		return (int) posts.stream().filter(post -> sourceType.equals(post.getSourceType())).count();
	}

	private FeedQuota calculateQuota(int size) {

		/*
		 * Very small pages should prioritize direct followed-user and own posts.
		 */
		if (size == 1) {
			return new FeedQuota(1, 0, 0);
		}

		if (size == 2) {
			return new FeedQuota(1, 1, 0);
		}

		if (size == 3) {
			return new FeedQuota(2, 1, 0);
		}

		if (size == 4) {
			return new FeedQuota(3, 1, 0);
		}

		int suggestedQuota = Math.max(1, Math.round(size * 0.10f));

		int ownQuota = Math.max(1, Math.round(size * 0.15f));

		int followingQuota = size - ownQuota - suggestedQuota;

		return new FeedQuota(followingQuota, ownQuota, suggestedQuota);
	}

	private void validateFeedRequest(UUID userId, int page, int size) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID is required");
		}

		if (page < 0) {
			throw new IllegalArgumentException("Page number cannot be negative");
		}

		if (size <= 0 || size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("Feed size must be between 1 and " + MAX_PAGE_SIZE);
		}
	}

	private long safeLong(Long value) {

		return value == null ? 0L : value;
	}

	private String normalizeUsername(String username) {

		if (username == null || username.isBlank()) {

			return "TrustNet User";
		}

		return username.trim();
	}

	/*
	 * This method belongs inside FeedServiceImpl, near the bottom of the class.
	 */
	private String getSuggestionReasonText(SuggestionReason reason) {

		if (reason == null) {
			return null;
		}

		return switch (reason) {

		case CONNECTION_FOLLOWS_THIS_PERSON -> "Suggested because a connection follows this person";

		case TOPIC_MATCH -> "Suggested because it matches a topic you interacted with";

		case TRUSTED_COMMUNITY_MEMBER -> "Suggested from a trusted community member";
		};
	}

	private record FeedQuota(int followingQuota, int ownQuota, int suggestedQuota) {
	}
}