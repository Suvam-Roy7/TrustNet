package com.social.FeedService.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.FeedService.Repository.FeedFollowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowDeletedEventConsumer {

	private final FeedFollowRepository feedFollowRepository;

	@KafkaListener(topics = "trustnet.follow.deleted.v1", groupId = "feed-service-follow-deleted")
	@Transactional
	public void consume(FollowDeletedEvent event) {

		log.info("Received FOLLOW_DELETED event. eventId={}, followerId={}, followedUserId={}", event.eventId(),
				event.followerId(), event.followedUserId());

		if (event.followerId() == null || event.followedUserId() == null) {

			log.warn("Invalid FOLLOW_DELETED event ignored. eventId={}", event.eventId());

			return;
		}

		long deletedRecords = feedFollowRepository.deleteByFollowerIdAndFollowedUserId(event.followerId(),
				event.followedUserId());

		if (deletedRecords == 0) {

			log.info("Follow edge was already absent. followerId={}, followedUserId={}", event.followerId(),
					event.followedUserId());

			return;
		}

		log.info("Follow edge removed from FeedService. followerId={}, followedUserId={}", event.followerId(),
				event.followedUserId());
	}
}