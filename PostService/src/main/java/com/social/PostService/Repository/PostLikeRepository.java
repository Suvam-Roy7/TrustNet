package com.social.PostService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

	boolean existsByPostIdAndUserId(UUID postId, UUID userId);

	long countByPostId(UUID postId);

	Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

	void deleteByPostId(UUID postId);
}
