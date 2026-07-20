package com.social.SocialGraphService.DTOs;

import java.util.UUID;

import lombok.Data;

@Data
public class ProfileResponseDTO {

    private UUID userId;

    private String username;
}
