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
import com.social.SocialGraphService.DTOs.FollowRequestResponseDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.NotificationResponseDTO;
import com.social.SocialGraphService.DTOs.SocialStatsDTO;
import com.social.SocialGraphService.DTOs.SocialSummaryResponseDTO;
import com.social.SocialGraphService.DTOs.UserResponseDTO;
import com.social.SocialGraphService.DTOs.ProfileResponseDTO;
import com.social.SocialGraphService.DTOs.RelationshipStatusResponseDTO;
import com.social.SocialGraphService.Entity.Follow;
import com.social.SocialGraphService.Entity.NotificationType;
import com.social.SocialGraphService.Event.FollowCreatedDomainEvent;
import com.social.SocialGraphService.Event.FollowCreatedEvent;
import com.social.SocialGraphService.Event.FollowDeletedEvent;
import com.social.SocialGraphService.Exception.FollowAlreadyExistsException;
import com.social.SocialGraphService.Exception.FollowNotFoundException;
import com.social.SocialGraphService.Exception.SelfFollowNotAllowedException;
import com.social.SocialGraphService.Exception.UserNotFoundException;
import com.social.SocialGraphService.Repository.FollowRepository;
import com.social.SocialGraphService.Repository.FollowRequestRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.social.SocialGraphService.Entity.FollowRequest;
import com.social.SocialGraphService.Entity.FollowRequestStatus;
import com.social.SocialGraphService.Exception.FollowRequestAlreadyExistsException;
import com.social.SocialGraphService.Exception.FollowRequestNotFoundException;
import com.social.SocialGraphService.Exception.InvalidFollowRequestStateException;
import com.social.SocialGraphService.Repository.FollowRequestRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialGraphServiceImpl implements SocialGraphService {

	private final FollowRepository repository;

	private final AuthClient authClient;

	private final NotificationClient notificationClient;

	private final ProfileClient profileClient;

	private final ApplicationEventPublisher eventPublisher;

	private final KafkaTemplate<String, FollowCreatedEvent> kafkaTemplate;

	private final KafkaTemplate<String, Object> kafkaTemplate2;

	private final FollowRequestRepository followRequestRepository;

	private UserResponseDTO validateUser(UUID userId) {

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {

			throw new RuntimeException("User account is inactive");
		}

		return user;
	}

	@Override
	@Transactional
	public FollowResponseDTO follow(UUID followerId, UUID followedUserId) {

		if (followerId == null || followedUserId == null) {
			throw new IllegalArgumentException("Follower ID and followed user ID are required");
		}

		if (followerId.equals(followedUserId)) {
			throw new IllegalArgumentException("A user cannot follow themselves");
		}

		if (repository.existsByFollowerIdAndFollowedUserId(followerId, followedUserId)) {

			throw new FollowAlreadyExistsException("Follow relationship already exists");
		}

		Follow follow = Follow.builder().followerId(followerId).followedUserId(followedUserId).createdAt(Instant.now())
				.build();

		Follow savedFollow = repository.save(follow);

		FollowCreatedEvent event = new FollowCreatedEvent(UUID.randomUUID(), followerId, followedUserId, Instant.now());

		kafkaTemplate.send("trustnet.follow.created.v1", followerId.toString(), event);

		return FollowResponseDTO.builder().id(savedFollow.getId()).followerId(savedFollow.getFollowerId())
				.followedUserId(savedFollow.getFollowedUserId()).createdAt(savedFollow.getCreatedAt()).build();
	}

	@Override
	@Transactional
	public void unfollow(UUID followerId, UUID followedUserId) {

		Follow follow = repository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
				.orElseThrow(() -> new FollowNotFoundException("Follow relationship not found"));

		repository.delete(follow);

		FollowDeletedEvent event = new FollowDeletedEvent(UUID.randomUUID(), followerId, followedUserId, Instant.now());

		kafkaTemplate2.send("trustnet.follow.deleted.v1", followerId.toString(), event);
	}

	@Override
	public List<UUID> getFollowers(UUID userId) {

		return repository.findByFollowedUserId(userId).stream().map(Follow::getFollowerId).toList();
	}

	@Override
	public List<UUID> getFollowing(UUID userId) {

		return repository.findByFollowerId(userId).stream().map(Follow::getFollowedUserId).toList();
	}

	@Override
	@Transactional
	public FollowRequestResponseDTO sendFollowRequest(UUID requesterId, UUID receiverId) {

		if (requesterId == null || receiverId == null) {
			throw new IllegalArgumentException("Requester ID and receiver ID are required");
		}

		if (requesterId.equals(receiverId)) {
			throw new IllegalArgumentException("A user cannot send a follow request to themselves");
		}

		boolean alreadyFollowing = repository.existsByFollowerIdAndFollowedUserId(requesterId, receiverId);

		/*
		 * If an active Follow row exists, a new request must not be created.
		 */
		if (alreadyFollowing) {
			throw new FollowRequestAlreadyExistsException("You are already following this user");
		}

		FollowRequest followRequest = followRequestRepository.findByRequesterIdAndReceiverId(requesterId, receiverId)
				.map(existingRequest -> {

					if (existingRequest.getStatus() == FollowRequestStatus.PENDING) {

						throw new FollowRequestAlreadyExistsException("A follow request is already pending");
					}

					/*
					 * If the previous request was ACCEPTED but the Follow row no longer exists, the
					 * user must have unfollowed.
					 *
					 * REJECTED and old ACCEPTED requests can therefore be reused.
					 */
					existingRequest.setStatus(FollowRequestStatus.PENDING);

					existingRequest.setCreatedAt(Instant.now());

					existingRequest.setRespondedAt(null);

					return existingRequest;
				}).orElseGet(() -> FollowRequest.builder().requesterId(requesterId).receiverId(receiverId)
						.status(FollowRequestStatus.PENDING).createdAt(Instant.now()).build());

		FollowRequest savedRequest = followRequestRepository.save(followRequest);

		createNotificationSafely(savedRequest.getReceiverId(), savedRequest.getRequesterId(),
				NotificationType.FOLLOW_REQUEST, "sent you a follow request.");

		return mapFollowRequestToDTO(savedRequest);
	}

	@Override
	@Transactional
	public List<FollowRequestResponseDTO> getIncomingFollowRequests(UUID receiverId) {

		if (receiverId == null) {
			throw new IllegalArgumentException("Receiver ID is required");
		}

		return followRequestRepository
				.findByReceiverIdAndStatusOrderByCreatedAtDesc(receiverId, FollowRequestStatus.PENDING).stream()
				.map(this::mapFollowRequestToDTO).toList();
	}

	@Override
	@Transactional
	public FollowRequestResponseDTO acceptFollowRequest(UUID receiverId, UUID requestId) {

		if (receiverId == null || requestId == null) {
			throw new IllegalArgumentException("Receiver ID and request ID are required");
		}

		FollowRequest followRequest = followRequestRepository.findForUpdate(requestId, receiverId)
				.orElseThrow(() -> new FollowRequestNotFoundException("Follow request not found"));

		if (followRequest.getStatus() != FollowRequestStatus.PENDING) {

			throw new InvalidFollowRequestStateException("Only a pending follow request can be accepted");
		}

		UUID requesterId = followRequest.getRequesterId();

		boolean relationshipExists = repository.existsByFollowerIdAndFollowedUserId(requesterId, receiverId);

		/*
		 * Protect against duplicate Follow rows.
		 */
		if (!relationshipExists) {

			Follow follow = Follow.builder().followerId(requesterId).followedUserId(receiverId).createdAt(Instant.now())
					.build();

			repository.save(follow);

			FollowCreatedEvent event = new FollowCreatedEvent(UUID.randomUUID(), requesterId, receiverId,
					Instant.now());

			kafkaTemplate.send("trustnet.follow.created.v1", requesterId.toString(), event);
		}

		followRequest.setStatus(FollowRequestStatus.ACCEPTED);

		followRequest.setRespondedAt(Instant.now());

		FollowRequest updatedRequest = followRequestRepository.save(followRequest);

		return mapFollowRequestToDTO(updatedRequest);
	}

	@Override
	@Transactional
	public FollowRequestResponseDTO rejectFollowRequest(UUID receiverId, UUID requestId) {

		if (receiverId == null || requestId == null) {
			throw new IllegalArgumentException("Receiver ID and request ID are required");
		}

		FollowRequest followRequest = followRequestRepository.findForUpdate(requestId, receiverId)
				.orElseThrow(() -> new FollowRequestNotFoundException("Follow request not found"));

		if (followRequest.getStatus() != FollowRequestStatus.PENDING) {

			throw new InvalidFollowRequestStateException("Only a pending follow request can be rejected");
		}

		followRequest.setStatus(FollowRequestStatus.REJECTED);

		followRequest.setRespondedAt(Instant.now());

		FollowRequest updatedRequest = followRequestRepository.save(followRequest);

		return mapFollowRequestToDTO(updatedRequest);
	}

	@Override
	@Transactional
	public SocialSummaryResponseDTO getSocialSummary(UUID userId) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID is required");
		}

		long followingCount = repository.countByFollowerId(userId);

		long followerCount = repository.countByFollowedUserId(userId);

		long pendingIncomingRequestCount = followRequestRepository.countByReceiverIdAndStatus(userId,
				FollowRequestStatus.PENDING);

		return SocialSummaryResponseDTO.builder().followingCount(followingCount).followerCount(followerCount)
				.pendingIncomingRequestCount(pendingIncomingRequestCount).build();
	}

	@Override
	@Transactional(readOnly = true)
	public RelationshipStatusResponseDTO getRelationshipStatus(UUID requesterId, UUID receiverId) {

		if (requesterId == null || receiverId == null) {
			throw new IllegalArgumentException("Requester ID and receiver ID are required");
		}

		boolean following = repository.existsByFollowerIdAndFollowedUserId(requesterId, receiverId);

		FollowRequestStatus requestStatus = followRequestRepository
				.findByRequesterIdAndReceiverId(requesterId, receiverId).map(FollowRequest::getStatus).orElse(null);

		boolean requestPending = !following && requestStatus == FollowRequestStatus.PENDING;

		return RelationshipStatusResponseDTO.builder().following(following).requestPending(requestPending)
				.requestStatus(requestStatus).build();
	}

	@Override
	public SocialStatsDTO getStats(UUID userId) {

		long followers = repository.countByFollowedUserId(userId);

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

	private FollowRequestResponseDTO mapFollowRequestToDTO(FollowRequest followRequest) {

		return FollowRequestResponseDTO.builder().requestId(followRequest.getId())
				.requesterId(followRequest.getRequesterId()).receiverId(followRequest.getReceiverId())
				.status(followRequest.getStatus()).createdAt(followRequest.getCreatedAt())
				.respondedAt(followRequest.getRespondedAt()).build();
	}

	private void createNotificationSafely(UUID recipientUserId, UUID actorUserId, NotificationType type,
			String message) {

		CreateNotificationRequestDTO request = CreateNotificationRequestDTO.builder().recipientUserId(recipientUserId)
				.actorUserId(actorUserId).type(type).message(message).build();

		try {

			NotificationResponseDTO response = notificationClient.createNotification(request);

			log.info("Notification created successfully: id={}, type={}, actor={}, recipient={}",
					response != null ? response.getId() : null, type, actorUserId, recipientUserId);

		} catch (FeignException exception) {

			log.error("NotificationService Feign call failed: status={}, response={}, type={}, actor={}, recipient={}",
					exception.status(), exception.contentUTF8(), type, actorUserId, recipientUserId, exception);

		} catch (Exception exception) {

			log.error("Notification creation failed: type={}, actor={}, recipient={}, reason={}", type, actorUserId,
					recipientUserId, exception.getMessage(), exception);
		}
	}
}
