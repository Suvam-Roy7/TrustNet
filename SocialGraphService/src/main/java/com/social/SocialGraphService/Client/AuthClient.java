package com.social.SocialGraphService.Client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.social.SocialGraphService.DTOs.UserResponseDTO;

@FeignClient(name = "AUTHSERVICE")
public interface AuthClient {

    @GetMapping("/api/auth/users/{userId}")
    UserResponseDTO  getUser(
            @PathVariable UUID userId);
    
    @GetMapping("/api/auth/users/{userId}/exists")
    Boolean userExists(
            @PathVariable("userId") UUID userId);
}
