package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.POST_UPDATED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostUpdatedEventPublisher {

	private final KafkaTemplate<String, PostUpdatedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(PostUpdatedDomainEvent domainEvent) {

		PostUpdatedEvent kafkaEvent = new PostUpdatedEvent(domainEvent.eventId(), domainEvent.postId(),
				domainEvent.authorId(), domainEvent.content(), domainEvent.updatedAt());

		kafkaTemplate.send(POST_UPDATED_TOPIC, domainEvent.postId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish POST_UPDATED event. eventId={}, postId={}", domainEvent.eventId(),
								domainEvent.postId(), exception);

						return;
					}

					log.info("POST_UPDATED event published. eventId={}, postId={}, partition={}, offset={}",
							domainEvent.eventId(), domainEvent.postId(), result.getRecordMetadata().partition(),
							result.getRecordMetadata().offset());
				});
	}
}