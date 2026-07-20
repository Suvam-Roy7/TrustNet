package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.POST_DELETED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostDeletedEventPublisher {

	private final KafkaTemplate<String, PostDeletedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(PostDeletedDomainEvent domainEvent) {

		PostDeletedEvent kafkaEvent = new PostDeletedEvent(domainEvent.eventId(), domainEvent.postId(),
				domainEvent.authorId(), domainEvent.occurredAt());

		kafkaTemplate.send(POST_DELETED_TOPIC, domainEvent.authorId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish POST_DELETED event. eventId={}, postId={}", domainEvent.eventId(),
								domainEvent.postId(), exception);

						return;
					}

					log.info("POST_DELETED event published. eventId={}, postId={}, partition={}, offset={}",
							domainEvent.eventId(), domainEvent.postId(), result.getRecordMetadata().partition(),
							result.getRecordMetadata().offset());
				});
	}
}