package com.social.FeedService.Entity;

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
@Table(name = "feed_posts", uniqueConstraints = {
		@UniqueConstraint(name = "uk_feed_post_event", columnNames = "event_id"),

		@UniqueConstraint(name = "uk_feed_post_id", columnNames = "post_id") })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPost {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "event_id", nullable = false, unique = true)
	private UUID eventId;

	@Column(name = "post_id", nullable = false, unique = true)
	private UUID postId;

	@Column(name = "author_id", nullable = false)
	private UUID authorId;

	@Column(name = "content", length = 5000)
	private String content;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	
	@Column(name = "updated_at")
	private Instant updatedAt;
}