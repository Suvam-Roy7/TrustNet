package com.social.AuthService.DTOs;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginRequestDTO {
	
	@Email
    private String email;

    private String password;
}