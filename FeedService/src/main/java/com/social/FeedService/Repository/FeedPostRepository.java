package com.social.FeedService.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.FeedService.Entity.FeedPost;

public interface FeedPostRepository extends JpaRepository<FeedPost, UUID> {

	boolean existsByEventId(UUID eventId);

	Page<FeedPost> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

	Page<FeedPost> findByAuthorIdInOrderByCreatedAtDesc(Collection<UUID> authorIds, Pageable pageable);

	/*
	 * Second-degree connection:
	 *
	 * Current user follows A A follows B Suggest recent posts created by B
	 *
	 * The current user and already-followed users are excluded.
	 */
	@Query("""
			SELECT DISTINCT post
			FROM FeedPost post
			WHERE post.authorId IN (
			    SELECT secondEdge.followedUserId
			    FROM FeedFollowEdge directEdge,
			         FeedFollowEdge secondEdge
			    WHERE directEdge.followerId = :userId
			      AND secondEdge.followerId =
			          directEdge.followedUserId
			)
			AND post.authorId NOT IN :excludedAuthorIds
			ORDER BY post.createdAt DESC
			""")
	List<FeedPost> findSecondDegreeSuggestedPosts(@Param("userId") UUID userId,

			@Param("excludedAuthorIds") Set<UUID> excludedAuthorIds,

			Pageable pageable);
}