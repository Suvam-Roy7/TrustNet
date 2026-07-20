package com.social.ProfileService.Controller;

import java.util.UUID;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.ProfileService.DTOs.CreateProfileRequestDTO;
import com.social.ProfileService.DTOs.UpdateProfileRequestDTO;
import com.social.ProfileService.Entity.Profile;
import com.social.ProfileService.Service.ProfileService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Profile> createProfile(
            @Valid @RequestBody CreateProfileRequestDTO request) {

        Profile profile = profileService.createProfile(request);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Profile> getProfile(
            @PathVariable UUID userId) {

        Profile profile = profileService.getProfile(userId);

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{userId}")
    @PreAuthorize(
            "hasRole('ADMIN') "
          + "or @profileSecurity.isOwner(#userId)")
    public ResponseEntity<Profile> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequestDTO request) {

        Profile profile =
                profileService.updateProfile(userId, request);

        return ResponseEntity.ok(profile);
    }
}