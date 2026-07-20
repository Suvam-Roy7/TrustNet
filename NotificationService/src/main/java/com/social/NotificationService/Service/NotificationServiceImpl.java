package com.social.NotificationService.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.social.NotificationService.DTOs.CreateNotificationRequestDTO;
import com.social.NotificationService.DTOs.NotificationResponseDTO;
import com.social.NotificationService.Entity.Notification;
import com.social.NotificationService.Exception.NotificationNotFoundException;
import com.social.NotificationService.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository repository;

	@Override
	public NotificationResponseDTO createNotification(CreateNotificationRequestDTO request) {

		Notification notification = Notification.builder().recipientUserId(request.getRecipientUserId())
				.actorUserId(request.getActorUserId()).type(request.getType()).message(request.getMessage()).build();

		Notification saved = repository.save(notification);

		return mapToDTO(saved);
	}

	@Override
	public Page<NotificationResponseDTO> getNotifications(int page, int size) {

		UUID userId = getLoggedInUserId();

		Pageable pageable = PageRequest.of(page, size);

		return repository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable).map(this::mapToDTO);
	}

	@Override
	public void markAsRead(UUID notificationId) {

		Notification notification = repository.findById(notificationId)
				.orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

		notification.setIsRead(true);

		repository.save(notification);
	}

	@Override
	public void deleteNotification(UUID notificationId) {

		Notification notification = repository.findById(notificationId)
				.orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

		repository.delete(notification);
	}

	@Override
	public long getUnreadCount() {

		UUID userId = getLoggedInUserId();

		return repository.countByRecipientUserIdAndIsReadFalse(userId);
	}

	@Override
	public void markAllAsRead() {

		UUID userId = getLoggedInUserId();

		List<Notification> notifications = repository.findByRecipientUserIdAndIsReadFalse(userId);

		notifications.forEach(notification -> notification.setIsRead(true));

		repository.saveAll(notifications);
	}

	private NotificationResponseDTO mapToDTO(Notification notification) {

		return NotificationResponseDTO.builder().id(notification.getId())
				.recipientUserId(notification.getRecipientUserId()).actorUserId(notification.getActorUserId())
				.type(notification.getType()).message(notification.getMessage()).isRead(notification.getIsRead())
				.createdAt(notification.getCreatedAt()).build();
	}

	private UUID getLoggedInUserId() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return UUID.fromString(auth.getName());
	}
}