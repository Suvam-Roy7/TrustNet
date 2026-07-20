package com.social.ProfileService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.ProfileService.Entity.Profile;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    Optional<Profile> findByUsername(String username);
}