package com.social.SocialGraphService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.SocialGraphService.Entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

	boolean existsByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

	Optional<Follow> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

	List<Follow> findByFollowedUserId(UUID followedUserId);

	List<Follow> findByFollowerId(UUID followerId);

	long countByFollowedUserId(UUID followedUserId);

	long countByFollowerId(UUID followerId);
}