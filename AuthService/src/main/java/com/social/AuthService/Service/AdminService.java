package com.social.AuthService.Service;

import java.util.UUID;

import com.social.AuthService.Entity.Role;

public interface AdminService {

	void suspendUser(UUID userId);

	void activateUser(UUID userId);

	void updateRole(UUID userId, Role role);
}