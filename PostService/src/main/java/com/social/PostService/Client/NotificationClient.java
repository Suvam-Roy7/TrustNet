package com.social.PostService.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.social.PostService.DTOs.CreateNotificationRequestDTO;
import com.social.PostService.DTOs.NotificationResponseDTO;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    NotificationResponseDTO createNotification(
            @RequestBody
            CreateNotificationRequestDTO request);
}
