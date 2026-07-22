package com.social.PostService.Client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.social.PostService.DTOs.UserResponseDTO;

@FeignClient(
        name = "AUTHSERVICE",
        path = "/api/auth"
)
public interface AuthClient {

    @GetMapping("/users/{userId}")
    UserResponseDTO getUser(
            @PathVariable("userId") UUID userId
    );
}