package com.social.SocialGraphService.Service;

import java.util.List;
import java.util.UUID;

import com.social.SocialGraphService.DTOs.FollowRequestDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.SocialStatsDTO;

public interface SocialGraphService {

	FollowResponseDTO follow(UUID followedUserId);

	void unfollow(FollowRequestDTO request);

	List<UUID> getFollowers(UUID userId);

	List<UUID> getFollowing(UUID userId);

	SocialStatsDTO getStats(UUID userId);
}
