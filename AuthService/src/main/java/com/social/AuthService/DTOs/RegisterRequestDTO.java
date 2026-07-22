package com.social.AuthService.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
	
	@NotBlank(message = "Username is required")
    @Size(
        min = 3,
        max = 30,
        message = "Username must be between 3 and 30 characters"
    )
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
