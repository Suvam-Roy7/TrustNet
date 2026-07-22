package com.social.FeedService.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feed_follow_edges", uniqueConstraints = {
		@UniqueConstraint(name = "uk_feed_follower_followed", columnNames = { "follower_id", "followed_user_id" }) })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedFollowEdge {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "follower_id", nullable = false)
	private UUID followerId;

	@Column(name = "followed_user_id", nullable = false)
	private UUID followedUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {

		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}