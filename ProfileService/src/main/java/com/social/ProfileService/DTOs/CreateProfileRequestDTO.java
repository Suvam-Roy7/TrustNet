package com.social.ProfileService.DTOs;

import lombok.Data;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateProfileRequestDTO {
	
	@NotNull(message = "User Id is required")
    private UUID userId;
	
	@NotBlank(message = "Username is required")
    private String username;
}