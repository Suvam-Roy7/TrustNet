package com.social.PostService.Event;

import static com.social.PostService.Config.KafkaTopicConfig.MENTION_CREATED_TOPIC;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MentionCreatedEventPublisher {

	private final KafkaTemplate<String, MentionCreatedEvent> kafkaTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(MentionCreatedDomainEvent domainEvent) {

		MentionCreatedEvent kafkaEvent = new MentionCreatedEvent(domainEvent.eventId(), domainEvent.postId(),
				domainEvent.actorUserId(), domainEvent.recipientUserId(), domainEvent.actorUsername(),
				domainEvent.occurredAt());

		kafkaTemplate.send(MENTION_CREATED_TOPIC, domainEvent.recipientUserId().toString(), kafkaEvent)
				.whenComplete((result, exception) -> {

					if (exception != null) {

						log.error("Failed to publish MENTION_CREATED event. eventId={}, postId={}",
								domainEvent.eventId(), domainEvent.postId(), exception);

						return;
					}

					log.info("MENTION_CREATED event published. eventId={}, partition={}, offset={}",
							domainEvent.eventId(), result.getRecordMetadata().partition(),
							result.getRecordMetadata().offset());
				});
	}
}