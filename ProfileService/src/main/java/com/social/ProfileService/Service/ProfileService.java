package com.social.ProfileService.Service;

import java.util.UUID;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.ProfileResponseDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;

public interface ProfileService {

    ProfileResponseDTO createProfile(
            CreateProfileRequestDTO request
    );

    ProfileResponseDTO getProfile(UUID userId);
    
    ProfileResponseDTO getProfileByUsername(
            String username
    );

    ProfileResponseDTO updateProfile(
            UUID userId,
            UpdateProfileRequestDTO request
    );
}
