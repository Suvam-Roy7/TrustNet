package com.social.SocialGraphService.DTOs;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponseDTO {

    private UUID id;

    private String email;

    private Boolean emailVerified;

    private String accountStatus;
}
