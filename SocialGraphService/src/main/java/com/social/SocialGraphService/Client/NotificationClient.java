package com.social.SocialGraphService.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.social.SocialGraphService.DTOs.CreateNotificationRequestDTO;
import com.social.SocialGraphService.DTOs.NotificationResponseDTO;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    NotificationResponseDTO createNotification(
            @RequestBody
            CreateNotificationRequestDTO request);
}