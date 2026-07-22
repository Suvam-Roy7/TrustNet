package com.social.ProfileService.Service;

import java.util.UUID;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.ProfileResponseDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;
import com.social.ProfileService.Entity.TrustLevel;
import com.social.ProfileService.Exception.ProfileAlreadyExistsException;
import com.social.ProfileService.Exception.ProfileNotFoundException;
import com.social.ProfileService.Repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	private final ProfileRepository repository;

	@Override
	@Transactional
	public ProfileResponseDTO createProfile(CreateProfileRequestDTO request) {

		repository.findByUserId(request.getUserId()).ifPresent(profile -> {
			throw new ProfileAlreadyExistsException("Profile already exists");
		});

		Profile profile = Profile.builder().userId(request.getUserId()).username(request.getUsername().trim())
				.trustLevel(TrustLevel.NEW_USER).build();

		Profile savedProfile = repository.save(profile);

		return mapToDTO(savedProfile);
	}

	@Override
	@Cacheable(value = "profiles", key = "#userId")
	@Transactional(readOnly = true)
	public ProfileResponseDTO getProfile(UUID userId) {

		Profile profile = repository.findByUserId(userId)
				.orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

		return mapToDTO(profile);
	}

	@Override
	@Transactional
	public ProfileResponseDTO getProfileByUsername(String username) {

		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Username is required");
		}

		Profile profile = repository.findByUsername(username.trim())
				.orElseThrow(() -> new ProfileNotFoundException("Profile not found for username: " + username));

		return mapToDTO(profile);
	}

	@Override
	@CachePut(value = "profiles", key = "#userId")
	@Transactional
	public ProfileResponseDTO updateProfile(UUID userId, UpdateProfileRequestDTO request) {

		Profile profile = repository.findByUserId(userId)
				.orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

		if (request.getUsername() != null && !request.getUsername().isBlank()) {

			profile.setUsername(request.getUsername().trim());
		}

		if (request.getBio() != null) {
			profile.setBio(request.getBio().trim());
		}

		if (request.getWebsite() != null) {
			profile.setWebsite(request.getWebsite().trim());
		}

		Profile updatedProfile = repository.save(profile);

		return mapToDTO(updatedProfile);
	}

	private ProfileResponseDTO mapToDTO(Profile profile) {

		return ProfileResponseDTO.builder().userId(profile.getUserId()).username(profile.getUsername())
				.bio(profile.getBio()).profilePictureUrl(profile.getProfilePictureUrl()).website(profile.getWebsite())
				.trustLevel(profile.getTrustLevel()).build();
	}
}