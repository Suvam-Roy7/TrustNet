package com.social.SocialGraphService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.SocialGraphService.Entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

	boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

	Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

	List<Follow> findByFollowerId(UUID followerId);

	List<Follow> findByFollowingId(UUID followingId);

	long countByFollowerId(UUID followerId);

	long countByFollowingId(UUID followingId);

	boolean existsByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

	Optional<Follow> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);
}
