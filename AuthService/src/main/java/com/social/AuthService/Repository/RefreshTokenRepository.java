package com.social.AuthService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.AuthService.Entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

	void deleteByUserId(UUID userId);
}
