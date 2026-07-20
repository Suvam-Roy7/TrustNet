package com.social.NotificationService.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.NotificationService.Entity.Notification;
import com.social.NotificationService.Entity.NotificationType;
import com.social.NotificationService.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MentionCreatedEventConsumer {

	private final NotificationRepository notificationRepository;

	@KafkaListener(topics = "trustnet.mention.created.v1", groupId = "notification-service-mention-created", containerFactory = "mentionCreatedKafkaListenerContainerFactory")
	@Transactional
	public void consume(MentionCreatedEvent event) {

		log.info("Received MENTION_CREATED event. eventId={}, postId={}", event.eventId(), event.postId());

		if (notificationRepository.existsByEventId(event.eventId())) {

			log.info("Duplicate MENTION_CREATED event ignored. eventId={}", event.eventId());

			return;
		}

		Notification notification = Notification.builder().eventId(event.eventId())
				.recipientUserId(event.recipientUserId()).actorUserId(event.actorUserId())
				.type(NotificationType.MENTION).message(event.actorUsername() + " mentioned you in a post")
				.isRead(false).build();

		notificationRepository.save(notification);

		log.info("MENTION notification saved. eventId={}, recipientUserId={}", event.eventId(),
				event.recipientUserId());
	}
}