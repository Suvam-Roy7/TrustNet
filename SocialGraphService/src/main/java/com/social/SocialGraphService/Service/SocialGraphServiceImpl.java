package com.social.SocialGraphService.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.social.SocialGraphService.Client.AuthClient;
import com.social.SocialGraphService.Client.NotificationClient;
import com.social.SocialGraphService.Client.ProfileClient;
import com.social.SocialGraphService.DTOs.CreateNotificationRequestDTO;
import com.social.SocialGraphService.DTOs.FollowRequestDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.SocialStatsDTO;
import com.social.SocialGraphService.DTOs.UserResponseDTO;
import com.social.SocialGraphService.DTOs.ProfileResponseDTO;
import com.social.SocialGraphService.Entity.Follow;
import com.social.SocialGraphService.Entity.NotificationType;
import com.social.SocialGraphService.Event.FollowCreatedDomainEvent;
import com.social.SocialGraphService.Exception.FollowAlreadyExistsException;
import com.social.SocialGraphService.Exception.FollowNotFoundException;
import com.social.SocialGraphService.Exception.SelfFollowNotAllowedException;
import com.social.SocialGraphService.Exception.UserNotFoundException;
import com.social.SocialGraphService.Repository.FollowRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialGraphServiceImpl implements SocialGraphService {

	private final FollowRepository repository;

	private final AuthClient authClient;

	private final NotificationClient notificationClient;

	private final ProfileClient profileClient;

	private final ApplicationEventPublisher eventPublisher;

	private UserResponseDTO validateUser(UUID userId) {

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {

			throw new RuntimeException("User account is inactive");
		}

		return user;
	}

	@Override
	@Transactional
	public FollowResponseDTO follow(UUID followedUserId) {

		UUID followerId = getLoggedInUserId();

		if (followerId.equals(followedUserId)) {
			throw new IllegalArgumentException("You cannot follow yourself");
		}

		if (!authClient.userExists(followedUserId)) {
			throw new UserNotFoundException("User to follow does not exist");
		}

		if (repository.existsByFollowerIdAndFollowedUserId(followerId, followedUserId)) {

			throw new FollowAlreadyExistsException("You are already following this user");
		}

		Follow follow = Follow.builder().followerId(followerId).followedUserId(followedUserId).createdAt(Instant.now())
				.build();

		Follow savedFollow = repository.save(follow);

		eventPublisher.publishEvent(
				new FollowCreatedDomainEvent(UUID.randomUUID(), followerId, followedUserId, Instant.now()));

		return mapToResponse(savedFollow);
	}

	@Override
	@Transactional
	public void unfollow(FollowRequestDTO request) {

		UUID followerId = getLoggedInUserId();

		Follow follow = repository.findByFollowerIdAndFollowedUserId(followerId, request.getFollowedUserId())
				.orElseThrow(() -> new FollowNotFoundException("Follow relationship not found"));

		repository.delete(follow);
	}

	@Override
	public List<UUID> getFollowers(UUID userId) {

		return repository.findByFollowingId(userId).stream().map(Follow::getFollowerId).collect(Collectors.toList());
	}

	@Override
	public List<UUID> getFollowing(UUID userId) {

		return repository.findByFollowerId(userId).stream().map(Follow::getFollowedUserId).collect(Collectors.toList());
	}

	@Override
	public SocialStatsDTO getStats(UUID userId) {

		long followers = repository.countByFollowingId(userId);

		long following = repository.countByFollowerId(userId);

		return new SocialStatsDTO(followers, following);
	}

	private UUID getLoggedInUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return UUID.fromString(authentication.getName());
	}

	private FollowResponseDTO mapToResponse(Follow follow) {

		return FollowResponseDTO.builder().id(follow.getId()).followerId(follow.getFollowerId())
				.followedUserId(follow.getFollowedUserId()).createdAt(follow.getCreatedAt()).build();
	}
}
