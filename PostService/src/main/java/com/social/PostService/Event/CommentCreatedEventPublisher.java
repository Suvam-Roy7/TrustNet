package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.COMMENT_CREATED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentCreatedEventPublisher {

	private final KafkaTemplate<String, CommentCreatedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(CommentCreatedDomainEvent domainEvent) {

		CommentCreatedEvent kafkaEvent = new CommentCreatedEvent(domainEvent.eventId(), domainEvent.commentId(),
				domainEvent.postId(), domainEvent.actorUserId(), domainEvent.recipientUserId(),
				domainEvent.actorUsername(), domainEvent.occurredAt());

		kafkaTemplate.send(COMMENT_CREATED_TOPIC, domainEvent.recipientUserId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish COMMENT_CREATED event. eventId={}, commentId={}",
								domainEvent.eventId(), domainEvent.commentId(), exception);

						return;
					}

					log.info("COMMENT_CREATED event published. eventId={}, partition={}, offset={}",
							domainEvent.eventId(), result.getRecordMetadata().partition(),
							result.getRecordMetadata().offset());
				});
	}
}