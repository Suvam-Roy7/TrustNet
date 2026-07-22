package com.social.ProfileService.DTOs;

import java.io.Serializable;
import java.util.UUID;

import com.social.ProfileService.Entity.TrustLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String username;
    private String bio;
    private String profilePictureUrl;
    private String website;
    private TrustLevel trustLevel;
}