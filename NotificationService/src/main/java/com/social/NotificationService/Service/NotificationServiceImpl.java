package com.social.NotificationService.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public NotificationResponseDTO createNotification(CreateNotificationRequestDTO request) {

		Notification notification = Notification.builder().eventId(UUID.randomUUID())
				.recipientUserId(request.getRecipientUserId()).actorUserId(request.getActorUserId())
				.type(request.getType()).message(request.getMessage()).isRead(false).build();

		Notification savedNotification = repository.save(notification);

		return mapToDTO(savedNotification);
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

	@Override
	@Transactional(readOnly = true)
	public Page<NotificationResponseDTO> getNotifications(UUID recipientUserId, int page, int size) {

		if (recipientUserId == null) {
			throw new IllegalArgumentException("Recipient user ID is required");
		}

		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 50);

		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

		return repository.findByRecipientUserId(recipientUserId, pageable).map(this::mapToDTO);
	}

	@Override
	@Transactional
	public NotificationResponseDTO markAsRead(UUID recipientUserId, UUID notificationId) {

		Notification notification = repository.findByIdAndRecipientUserId(notificationId, recipientUserId)
				.orElseThrow(() -> new RuntimeException("Notification not found"));

		if (!Boolean.TRUE.equals(notification.getIsRead())) {

			notification.setIsRead(true);

			notification = repository.save(notification);
		}

		return mapToDTO(notification);
	}

	@Override
	@Transactional(readOnly = true)
	public long getUnreadCount(UUID recipientUserId) {

		return repository.countByRecipientUserIdAndIsReadFalse(recipientUserId);
	}

	private NotificationResponseDTO mapToDTO(Notification notification) {

		return NotificationResponseDTO.builder().id(notification.getId())
				.recipientUserId(notification.getRecipientUserId()).actorUserId(notification.getActorUserId())
				.type(notification.getType()).message(notification.getMessage())
				.isRead(Boolean.TRUE.equals(notification.getIsRead())).createdAt(notification.getCreatedAt()).build();
	}

	private UUID getLoggedInUserId() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return UUID.fromString(auth.getName());
	}
}