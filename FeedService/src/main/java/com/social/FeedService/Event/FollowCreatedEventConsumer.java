package com.social.FeedService.Event;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.FeedService.Entity.FeedFollowEdge;
import com.social.FeedService.Repository.FeedFollowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowCreatedEventConsumer {

	private final FeedFollowRepository feedFollowRepository;

	@KafkaListener(topics = "trustnet.follow.created.v1", groupId = "feed-service-follow-created")
	@Transactional
	public void consume(FollowCreatedEvent event) {

		log.info("Received FOLLOW_CREATED event. eventId={}, followerId={}, followedUserId={}", event.eventId(),
				event.followerId(), event.followedUserId());

		if (event.followerId() == null || event.followedUserId() == null) {

			log.warn("Invalid FOLLOW_CREATED event ignored. eventId={}", event.eventId());

			return;
		}

		boolean relationshipAlreadyExists = feedFollowRepository.existsByFollowerIdAndFollowedUserId(event.followerId(),
				event.followedUserId());

		if (relationshipAlreadyExists) {

			log.info("Duplicate follow relationship ignored. followerId={}, followedUserId={}", event.followerId(),
					event.followedUserId());

			return;
		}

		LocalDateTime createdAt = event.createdAt() == null ? LocalDateTime.now()
				: LocalDateTime.ofInstant(event.createdAt(), ZoneOffset.UTC);

		FeedFollowEdge edge = FeedFollowEdge.builder().followerId(event.followerId())
				.followedUserId(event.followedUserId()).createdAt(createdAt).build();

		feedFollowRepository.save(edge);

		log.info("Follow edge saved in FeedService. followerId={}, followedUserId={}", event.followerId(),
				event.followedUserId());
	}
}