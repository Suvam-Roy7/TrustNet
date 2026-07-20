package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.POST_CREATED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreatedEventPublisher {

	private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(PostCreatedDomainEvent domainEvent) {

		PostCreatedEvent kafkaEvent = new PostCreatedEvent(domainEvent.eventId(), domainEvent.postId(),
				domainEvent.authorId(), domainEvent.content(), domainEvent.createdAt());

		kafkaTemplate.send(POST_CREATED_TOPIC, domainEvent.authorId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish POST_CREATED event. eventId={}", domainEvent.eventId(), exception);

						return;
					}

					log.info("POST_CREATED event published. eventId={}, partition={}, offset={}", domainEvent.eventId(),
							result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
				});
	}
}