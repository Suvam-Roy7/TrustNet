package com.social.NotificationService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.NotificationService.Entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

	long countByRecipientUserIdAndIsReadFalse(UUID recipientUserId);

	List<Notification> findByRecipientUserIdAndIsReadFalse(UUID userId);

	boolean existsByEventId(UUID eventId);

	Page<Notification> findByRecipientUserId(UUID recipientUserId, Pageable pageable);

	Optional<Notification> findByIdAndRecipientUserId(UUID notificationId, UUID recipientUserId);

}
