package com.social.ProfileService.Service;

import java.util.UUID;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;

public interface ProfileService {
	
    Profile createProfile(CreateProfileRequestDTO request);

    Profile getProfile(UUID userId);

    Profile updateProfile(UUID userId,
                          UpdateProfileRequestDTO request);

}
