package com.social.ProfileService.DTOs;

import java.util.UUID;

import com.social.ProfileService.Entity.TrustLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDTO {

    private UUID userId;

    private String username;

    private String bio;

    private TrustLevel trustLevel;
}
