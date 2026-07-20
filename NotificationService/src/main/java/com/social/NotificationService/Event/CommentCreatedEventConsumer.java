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
public class CommentCreatedEventConsumer {

	private final NotificationRepository notificationRepository;

	@KafkaListener(topics = "trustnet.comment.created.v1", groupId = "notification-service-comment-created", containerFactory = "commentCreatedKafkaListenerContainerFactory")
	@Transactional
	public void consume(CommentCreatedEvent event) {

		log.info("Received COMMENT_CREATED event. eventId={}, commentId={}", event.eventId(), event.commentId());

		if (notificationRepository.existsByEventId(event.eventId())) {

			log.info("Duplicate COMMENT_CREATED event ignored. eventId={}", event.eventId());

			return;
		}

		Notification notification = Notification.builder().eventId(event.eventId())
				.recipientUserId(event.recipientUserId()).actorUserId(event.actorUserId())
				.type(NotificationType.COMMENT).message(event.actorUsername() + " commented on your post").isRead(false)
				.build();

		notificationRepository.save(notification);

		log.info("COMMENT notification saved. eventId={}, recipientUserId={}", event.eventId(),
				event.recipientUserId());
	}
}