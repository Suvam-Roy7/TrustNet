package com.social.PostService.DTOs;

import java.util.UUID;

import lombok.Data;

@Data
public class ProfileResponseDTO {

    private UUID userId;

    private String username;

    private String bio;

    private String trustLevel;
}
