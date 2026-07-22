package com.social.NotificationService.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.NotificationService.Entity.Notification;
import com.social.NotificationService.Entity.NotificationType;
import com.social.NotificationService.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FollowCreatedEventConsumer {

	private final NotificationRepository notificationRepository;

	@KafkaListener(topics = "trustnet.follow.created.v1", groupId = "notification-service-follow-created", containerFactory = "followCreatedKafkaListenerContainerFactory")
	@Transactional
	public void consume(FollowCreatedEvent event) {

		if (notificationRepository.existsByEventId(event.eventId())) {

			return;
		}

		Notification notification = Notification.builder().eventId(event.eventId())

				/*
				 * The original requester receives the accepted notification.
				 */
				.recipientUserId(event.followerId())

				/*
				 * The receiver accepted the request.
				 */
				.actorUserId(event.followedUserId())

				.type(NotificationType.FOLLOW_ACCEPTED)

				.message("accepted your follow request.")

				.isRead(false).build();

		notificationRepository.save(notification);
	}
}