package com.social.SocialGraphService.Event;

import static com.social.SocialGraphService.Config.KafkaTopicConfig.FOLLOW_CREATED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowCreatedEventPublisher {

	private final KafkaTemplate<String, FollowCreatedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(FollowCreatedDomainEvent domainEvent) {

		FollowCreatedEvent kafkaEvent = new FollowCreatedEvent(domainEvent.eventId(), domainEvent.followerId(),
				domainEvent.followedUserId(), domainEvent.occurredAt());

		kafkaTemplate.send(FOLLOW_CREATED_TOPIC, domainEvent.followedUserId().toString(), kafkaEvent);
	}
}