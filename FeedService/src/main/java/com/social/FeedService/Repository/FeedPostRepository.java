package com.social.FeedService.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.social.FeedService.Entity.FeedPost;

public interface FeedPostRepository extends JpaRepository<FeedPost, UUID> {

	boolean existsByEventId(UUID eventId);

	boolean existsByPostId(UUID postId);

	long deleteByPostId(UUID postId);

	Page<FeedPost> findByAuthorIdInOrderByCreatedAtDesc(Collection<UUID> authorIds, Pageable pageable);
	
	Optional<FeedPost> findByPostId(UUID postId);
}