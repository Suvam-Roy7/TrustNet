package com.social.SocialGraphService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.SocialGraphService.Entity.FollowRequest;
import com.social.SocialGraphService.Entity.FollowRequestStatus;

import jakarta.persistence.LockModeType;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, UUID> {

	Optional<FollowRequest> findByRequesterIdAndReceiverId(UUID requesterId, UUID receiverId);

	Optional<FollowRequest> findByRequesterIdAndReceiverIdAndStatus(UUID requesterId, UUID receiverId,
			FollowRequestStatus status);

	boolean existsByRequesterIdAndReceiverIdAndStatus(UUID requesterId, UUID receiverId, FollowRequestStatus status);

	List<FollowRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(UUID receiverId, FollowRequestStatus status);

	List<FollowRequest> findByRequesterIdAndStatusOrderByCreatedAtDesc(UUID requesterId, FollowRequestStatus status);

	long countByReceiverIdAndStatus(UUID receiverId, FollowRequestStatus status);

	/*
	 * Locks the request while accepting or rejecting it, preventing two
	 * simultaneous responses.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT request
			FROM FollowRequest request
			WHERE request.id = :requestId
			  AND request.receiverId = :receiverId
			""")
	Optional<FollowRequest> findForUpdate(@Param("requestId") UUID requestId, @Param("receiverId") UUID receiverId);
}