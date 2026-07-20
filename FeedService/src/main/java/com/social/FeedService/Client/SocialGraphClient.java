package com.social.FeedService.Client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SOCIALGRAPHSERVICE")
public interface SocialGraphClient {

	@GetMapping("/api/social-graph/users/{userId}")
	List<UUID> getFollowing(@PathVariable("userId") UUID userId);
}
