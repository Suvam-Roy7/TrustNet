package com.social.ProfileService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.ProfileService.Entity.Profile;

public interface ProfileRepository
        extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    Optional<Profile> findByUsername(String username);

    boolean existsByUserId(UUID userId);

    boolean existsByUsername(String username);
}