package com.social.AuthService.Service;

import java.util.UUID;

import com.social.AuthService.DTOs.AuthResponseDTO;
import com.social.AuthService.DTOs.LoginRequestDTO;
import com.social.AuthService.DTOs.RegisterRequestDTO;
import com.social.AuthService.DTOs.UserResponseDTO;
import com.social.AuthService.Entity.Role;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
	
	AuthResponseDTO login(LoginRequestDTO request);

	void register(RegisterRequestDTO request);

	void logout(String authorizationHeader, String refreshToken);

	AuthResponseDTO refreshToken(String refreshToken);

	Boolean userExists(UUID userId);

	UserResponseDTO getUserById(UUID userId);

	void suspendUser(UUID userId);

	void activateUser(UUID userId);

	void updateRole(UUID userId, Role role);
}