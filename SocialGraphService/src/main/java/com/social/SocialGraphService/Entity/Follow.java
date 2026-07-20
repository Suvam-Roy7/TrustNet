package com.social.SocialGraphService.Entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows", uniqueConstraints = {
		@UniqueConstraint(name = "uk_follower_followed", columnNames = { "follower_id", "followed_user_id" }) })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "follower_id", nullable = false)
	private UUID followerId;

	@Column(name = "followed_user_id", nullable = false)
	private UUID followedUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
}