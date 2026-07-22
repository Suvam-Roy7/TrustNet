package com.social.ProfileService.Controller;

import java.util.UUID;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.ProfileResponseDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;
import com.social.ProfileService.Service.ProfileService;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	@PostMapping
	public ResponseEntity<ProfileResponseDTO> createProfile(@Valid @RequestBody CreateProfileRequestDTO request) {

		ProfileResponseDTO profile = profileService.createProfile(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(profile);
	}

	@GetMapping("/{userId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProfileResponseDTO> getProfile(@PathVariable UUID userId) {

		ProfileResponseDTO profile = profileService.getProfile(userId);

		return ResponseEntity.ok(profile);
	}

	@GetMapping("/username/{username}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProfileResponseDTO> getProfileByUsername(@PathVariable("username") String username) {

		ProfileResponseDTO response = profileService.getProfileByUsername(username);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{userId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProfileResponseDTO> updateProfile(@PathVariable UUID userId,
			@Valid @RequestBody UpdateProfileRequestDTO request) {

		return ResponseEntity.ok(profileService.updateProfile(userId, request));
	}
}