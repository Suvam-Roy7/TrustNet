package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.POST_LIKED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostLikedEventPublisher {

	private final KafkaTemplate<String, PostLikedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(PostLikedDomainEvent domainEvent) {

		PostLikedEvent kafkaEvent = new PostLikedEvent(domainEvent.eventId(), domainEvent.postId(),
				domainEvent.actorUserId(), domainEvent.recipientUserId(), domainEvent.actorUsername(),
				domainEvent.occurredAt());

		kafkaTemplate.send(POST_LIKED_TOPIC, domainEvent.recipientUserId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish POST_LIKED event. eventId={}, postId={}", domainEvent.eventId(),
								domainEvent.postId(), exception);

						return;
					}

					log.info("POST_LIKED event published. eventId={}, partition={}, offset={}", domainEvent.eventId(),
							result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
				});
	}
}