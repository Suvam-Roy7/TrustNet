package com.social.NotificationService.Service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.social.NotificationService.DTOs.CreateNotificationRequestDTO;
import com.social.NotificationService.DTOs.NotificationResponseDTO;

public interface NotificationService {

	NotificationResponseDTO createNotification(CreateNotificationRequestDTO request);

	Page<NotificationResponseDTO> getNotifications(int page, int size);

	void markAsRead(UUID notificationId);

	void deleteNotification(UUID notificationId);

	long getUnreadCount();

	void markAllAsRead();
}