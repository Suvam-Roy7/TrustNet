package com.social.AuthService.Service;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.social.AuthService.Client.ProfileClient;
import com.social.AuthService.DTOs.AuthResponseDTO;
import com.social.AuthService.DTOs.CreateProfileRequestDTO;
import com.social.AuthService.DTOs.LoginRequestDTO;
import com.social.AuthService.DTOs.RegisterRequestDTO;
import com.social.AuthService.DTOs.UserResponseDTO;
import com.social.AuthService.Entity.AccountStatus;
import com.social.AuthService.Entity.RefreshToken;
import com.social.AuthService.Entity.Role;
import com.social.AuthService.Entity.User;
import com.social.AuthService.Exception.InvalidCredentialsException;
import com.social.AuthService.Exception.UserAccountSuspendedException;
import com.social.AuthService.Exception.UserAlreadyExistsException;
import com.social.AuthService.Exception.UserNotFoundException;
import com.social.AuthService.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Date;

import org.springframework.security.core.context.SecurityContextHolder;

import com.social.AuthService.Exception.InvalidAccessTokenException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	private final RedisTemplate<String, String> redisTemplate;

	private final TokenBlacklistService tokenBlacklistService;

	private final RefreshTokenService refreshTokenService;

	private final ProfileClient profileClient;

	@Override
	@Transactional
	public void register(RegisterRequestDTO request) {

		String normalizedEmail = request.getEmail().trim().toLowerCase();

		String normalizedUsername = request.getUsername().trim();

		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new UserAlreadyExistsException("Email already registered");
		}

		User user = User.builder().email(normalizedEmail).password(passwordEncoder.encode(request.getPassword()))
				.emailVerified(false).accountStatus(AccountStatus.ACTIVE).role(Role.USER).build();

		User savedUser = userRepository.save(user);

		CreateProfileRequestDTO profileRequest = new CreateProfileRequestDTO();

		profileRequest.setUserId(savedUser.getId());
		profileRequest.setUsername(normalizedUsername);

		profileClient.createProfile(profileRequest);
	}

	@Override
	@Transactional
	public AuthResponseDTO login(LoginRequestDTO request) {

		try {

			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		} catch (Exception ex) {

			throw new InvalidCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (user.getAccountStatus() != AccountStatus.ACTIVE) {

			throw new UserAccountSuspendedException("User account is suspended");
		}

		String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

		String refreshToken = refreshTokenService.createRefreshToken(user.getId());

		return new AuthResponseDTO(accessToken, refreshToken);
	}

	@Override
	public Boolean userExists(UUID userId) {

		return userRepository.existsById(userId);
	}

	@Override
	public UserResponseDTO getUserById(UUID userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		return UserResponseDTO.builder().id(user.getId()).email(user.getEmail()).emailVerified(user.getEmailVerified())
				.accountStatus(user.getAccountStatus()).build();
	}

	@Transactional
	@Override
	public void suspendUser(UUID userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setAccountStatus(AccountStatus.SUSPENDED);

		userRepository.save(user);
	}

	@Transactional
	@Override
	public void activateUser(UUID userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setAccountStatus(AccountStatus.ACTIVE);

		userRepository.save(user);
	}

	@Transactional
	@Override
	public void updateRole(UUID userId, Role role) {

		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setRole(role);

		userRepository.save(user);
	}

	@Override
	@Transactional
	public void logout(String authorizationHeader, String rawRefreshToken) {

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

			throw new InvalidAccessTokenException("Authorization token is missing or invalid");
		}

		String accessToken = authorizationHeader.substring(7);

		Date expiration = jwtService.extractExpiration(accessToken);

		long remainingTtlMillis = expiration.getTime() - System.currentTimeMillis();

		if (remainingTtlMillis > 0) {

			tokenBlacklistService.blacklistToken(accessToken, remainingTtlMillis);
		}

		refreshTokenService.revokeRefreshToken(rawRefreshToken);

		SecurityContextHolder.clearContext();
	}

	@Override
	@Transactional
	public AuthResponseDTO refreshToken(String rawRefreshToken) {

		RefreshToken storedRefreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);

		User user = userRepository.findById(storedRefreshToken.getUserId())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (user.getAccountStatus() != AccountStatus.ACTIVE) {

			throw new UserAccountSuspendedException("User account is suspended");
		}

		/*
		 * Rotation: createRefreshToken() removes the user's previous token and stores a
		 * new one.
		 */
		String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

		String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

		return new AuthResponseDTO(newAccessToken, newRefreshToken);
	}
	
	
}
