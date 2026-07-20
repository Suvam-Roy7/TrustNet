package com.social.AuthService.DTOs;

import com.social.AuthService.Entity.Role;

import lombok.Data;

@Data
public class UpdateRoleRequestDTO {

    private Role role;
}
