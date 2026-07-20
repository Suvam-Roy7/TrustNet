package com.social.ProfileService.Service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;
import com.social.ProfileService.Entity.TrustLevel;
import com.social.ProfileService.Repository.ProfileRepository;
import com.social.ProfileService.Exception.ProfileAlreadyExistsException;
import com.social.ProfileService.Exception.ProfileNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
	
	@Autowired
	private final ProfileRepository repository;

    @Override
    public Profile createProfile(CreateProfileRequestDTO request) {
    	
    	repository.findByUserId(request.getUserId())
        .ifPresent(profile -> {
            throw new ProfileAlreadyExistsException(
                    "Profile already exists");
        });

        Profile profile = Profile.builder()
                .userId(request.getUserId())
                .username(request.getUsername())
                .trustLevel(TrustLevel.NEW_USER)
                .build();
        
        return repository.save(profile);
    }

    @Override
    @Cacheable(
    	    value = "profiles",
    	    key = "#userId")
    public Profile getProfile(UUID userId) {

        return repository.findByUserId(userId)
                .orElseThrow(() ->
                new ProfileNotFoundException("Profile not found for userId: " + userId));
    }
    
    @Override
    @CachePut(
    	    value = "profiles",
    	    key = "#userId")
    public Profile updateProfile(UUID userId,
                                 UpdateProfileRequestDTO request) {

        Profile profile = repository.findByUserId(userId)
                .orElseThrow(() ->
                new ProfileNotFoundException("Profile not found for userId: " + userId));

        if(request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }

        if(request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if(request.getProfession() != null) {
            profile.setProfession(request.getProfession());
        }

        if(request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }

        if(request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite());
        }

        if(request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        if(request.getCoverPictureUrl() != null) {
            profile.setCoverPictureUrl(request.getCoverPictureUrl());
        }

        return repository.save(profile);
    }
}
