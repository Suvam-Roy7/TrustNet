package com.social.AuthService.Controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.AuthService.DTOs.UpdateRoleRequestDTO;
import com.social.AuthService.Service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @PutMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> suspendUser(
            @PathVariable UUID userId) {

        service.suspendUser(userId);

        return ResponseEntity.ok(
                "User suspended successfully");
    }

    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(
            @PathVariable UUID userId) {

        service.activateUser(userId);

        return ResponseEntity.ok(
                "User activated successfully");
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateRole(

            @PathVariable UUID userId,

            @Valid
            @RequestBody UpdateRoleRequestDTO request) {

        service.updateRole(
                userId,
                request.getRole());

        return ResponseEntity.ok(
                "Role updated successfully");
    }
}