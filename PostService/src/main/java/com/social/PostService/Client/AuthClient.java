package com.social.PostService.Client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.social.PostService.DTOs.UserResponseDTO;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/users/{userId}")
    UserResponseDTO getUser(
            @PathVariable UUID userId);
}
