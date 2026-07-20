package com.social.SocialGraphService.Client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.social.SocialGraphService.DTOs.ProfileResponseDTO;

@FeignClient(name = "profile-service")
public interface ProfileClient {

    @GetMapping("/api/profiles/{userId}")
    ProfileResponseDTO getProfile(
            @PathVariable UUID userId);
}
