package com.social.SocialGraphService.Entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "follow_requests", uniqueConstraints = {
		@UniqueConstraint(name = "uk_follow_request_users", columnNames = { "requester_id",
				"receiver_id" }) }, indexes = {
						@Index(name = "idx_follow_request_receiver_status", columnList = "receiver_id,status"),
						@Index(name = "idx_follow_request_requester_status", columnList = "requester_id,status") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	/*
	 * User who sends the follow request.
	 */
	@Column(name = "requester_id", nullable = false)
	private UUID requesterId;

	/*
	 * User who receives the follow request.
	 */
	@Column(name = "receiver_id", nullable = false)
	private UUID receiverId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private FollowRequestStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "responded_at")
	private Instant respondedAt;

	@PrePersist
	public void prePersist() {

		if (status == null) {
			status = FollowRequestStatus.PENDING;
		}

		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}