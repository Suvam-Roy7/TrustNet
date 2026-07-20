package com.social.AuthService.DTOs;

import java.util.UUID;

import com.social.AuthService.Entity.AccountStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private UUID id;

    private String email;

    private Boolean emailVerified;

    private AccountStatus accountStatus;
}