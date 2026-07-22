package com.social.SocialGraphService.Service;

import java.util.List;
import java.util.UUID;

import com.social.SocialGraphService.DTOs.FollowRequestDTO;
import com.social.SocialGraphService.DTOs.FollowRequestResponseDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.RelationshipStatusResponseDTO;
import com.social.SocialGraphService.DTOs.SocialStatsDTO;
import com.social.SocialGraphService.DTOs.SocialSummaryResponseDTO;

public interface SocialGraphService {

	FollowResponseDTO follow(UUID followerId, UUID followedUserId);

	void unfollow(UUID followerId, UUID followedUserId);

	List<UUID> getFollowers(UUID userId);

	List<UUID> getFollowing(UUID userId);

	SocialStatsDTO getStats(UUID userId);

	FollowRequestResponseDTO sendFollowRequest(UUID requesterId, UUID receiverId);

	List<FollowRequestResponseDTO> getIncomingFollowRequests(UUID receiverId);

	FollowRequestResponseDTO acceptFollowRequest(UUID receiverId, UUID requestId);

	FollowRequestResponseDTO rejectFollowRequest(UUID receiverId, UUID requestId);

	SocialSummaryResponseDTO getSocialSummary(UUID userId);

	RelationshipStatusResponseDTO getRelationshipStatus(UUID requesterId, UUID receiverId);

}
