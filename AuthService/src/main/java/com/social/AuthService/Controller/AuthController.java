package com.social.AuthService.Controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.AuthService.DTOs.AuthResponseDTO;
import com.social.AuthService.DTOs.LoginRequestDTO;
import com.social.AuthService.DTOs.LogoutRequestDTO;
import com.social.AuthService.DTOs.RefreshRequestDTO;
import com.social.AuthService.DTOs.RegisterRequestDTO;
import com.social.AuthService.DTOs.UpdateRoleRequestDTO;
import com.social.AuthService.DTOs.UserResponseDTO;
import com.social.AuthService.Exception.RateLimitExceededException;
import com.social.AuthService.Service.AuthService;
import com.social.AuthService.Service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	private final RateLimitService rateLimitService;

	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {

		authService.register(request);

		return ResponseEntity.ok("User Registered Successfully");
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponseDTO> login(
	        @Valid @RequestBody LoginRequestDTO request) {

	    return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/logout")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader,

			@Valid @RequestBody LogoutRequestDTO request) {

		authService.logout(authorizationHeader, request.getRefreshToken());

		return ResponseEntity.ok("Logged out successfully");
	}

	@GetMapping("/users/{userId}/exists")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Boolean> userExists(@PathVariable UUID userId) {

		return ResponseEntity.ok(authService.userExists(userId));
	}

	@GetMapping("/users/{userId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID userId) {

		return ResponseEntity.ok(authService.getUserById(userId));
	}

	@PutMapping("/users/{userId}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> suspendUser(@PathVariable UUID userId) {

		authService.suspendUser(userId);

		return ResponseEntity.ok("User suspended successfully");
	}

	@PutMapping("/users/{userId}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> activateUser(@PathVariable UUID userId) {

		authService.activateUser(userId);

		return ResponseEntity.ok("User activated successfully");
	}

	@PutMapping("/users/{userId}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateRole(

			@PathVariable UUID userId,

			@Valid @RequestBody UpdateRoleRequestDTO request) {

		authService.updateRole(userId, request.getRole());

		return ResponseEntity.ok("Role updated successfully");
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshRequestDTO request) {

		return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
	}
}