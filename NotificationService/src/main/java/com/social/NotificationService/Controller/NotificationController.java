package com.social.NotificationService.Controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.NotificationService.DTOs.CreateNotificationRequestDTO;
import com.social.NotificationService.DTOs.NotificationResponseDTO;
import com.social.NotificationService.Service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService service;

	/*
	 * Create a notification. Used internally by other microservices.
	 */
	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<NotificationResponseDTO> createNotification(
			@Valid @RequestBody CreateNotificationRequestDTO request) {

		NotificationResponseDTO response = service.createNotification(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * Get notifications belonging to the authenticated user.
	 */
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
			@RequestHeader("X-User-Id") UUID recipientUserId,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "20") int size) {

		return ResponseEntity.ok(service.getNotifications(recipientUserId, page, size));
	}

	/*
	 * Mark one notification as read.
	 */
	@PatchMapping("/{notificationId}/read")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<NotificationResponseDTO> markAsRead(@RequestHeader("X-User-Id") UUID recipientUserId,

			@PathVariable("notificationId") UUID notificationId) {

		return ResponseEntity.ok(service.markAsRead(recipientUserId, notificationId));
	}

	/*
	 * Return unread count for the authenticated user.
	 *
	 * There must be only one method mapped to /unread-count.
	 */
	@GetMapping("/unread-count")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Long> getUnreadCount(@RequestHeader("X-User-Id") UUID recipientUserId) {

		return ResponseEntity.ok(service.getUnreadCount(recipientUserId));
	}
}