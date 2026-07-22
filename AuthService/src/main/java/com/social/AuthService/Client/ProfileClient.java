package com.social.AuthService.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.social.AuthService.DTOs.CreateProfileRequestDTO;

@FeignClient(
        name = "PROFILESERVICE",
        path = "/api/profiles"
)
public interface ProfileClient {

    @PostMapping
    void createProfile(
            @RequestBody CreateProfileRequestDTO request
    );
}