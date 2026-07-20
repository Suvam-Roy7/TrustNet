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
public class PostLikedEventConsumer {

	private final NotificationRepository notificationRepository;

	@KafkaListener(topics = "trustnet.post.liked.v1", groupId = "notification-service-post-liked", containerFactory = "postLikedKafkaListenerContainerFactory")
	@Transactional
	public void consume(PostLikedEvent event) {

		log.info("Received POST_LIKED event. eventId={}, postId={}", event.eventId(), event.postId());

		if (notificationRepository.existsByEventId(event.eventId())) {

			log.info("Duplicate POST_LIKED event ignored. eventId={}", event.eventId());

			return;
		}

		Notification notification = Notification.builder().eventId(event.eventId())
				.recipientUserId(event.recipientUserId()).actorUserId(event.actorUserId()).type(NotificationType.LIKE)
				.message(event.actorUsername() + " liked your post").isRead(false).build();

		notificationRepository.save(notification);

		log.info("LIKE notification saved. eventId={}, recipientUserId={}", event.eventId(), event.recipientUserId());
	}
}