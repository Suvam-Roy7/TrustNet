package com.social.FeedService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.FeedService.Entity.FeedFollowEdge;

public interface FeedFollowRepository extends JpaRepository<FeedFollowEdge, UUID> {

	@Query("""
			SELECT edge.followedUserId
			FROM FeedFollowEdge edge
			WHERE edge.followerId = :followerId
			""")
	List<UUID> findFollowedUserIdsByFollowerId(@Param("followerId") UUID followerId);

	boolean existsByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

	@Modifying
	long deleteByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);
}