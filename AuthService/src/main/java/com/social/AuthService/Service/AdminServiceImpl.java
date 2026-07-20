package com.social.AuthService.Service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.social.AuthService.Entity.AccountStatus;
import com.social.AuthService.Entity.Role;
import com.social.AuthService.Entity.User;
import com.social.AuthService.Exception.UserNotFoundException;
import com.social.AuthService.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

	private final UserRepository repository;

	@Override
	public void suspendUser(UUID userId) {

		User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setAccountStatus(AccountStatus.SUSPENDED);

		repository.save(user);
	}

	@Override
	public void activateUser(UUID userId) {

		User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setAccountStatus(AccountStatus.ACTIVE);

		repository.save(user);
	}

	@Override
	public void updateRole(UUID userId, Role role) {

		User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setRole(role);

		repository.save(user);
	}
}