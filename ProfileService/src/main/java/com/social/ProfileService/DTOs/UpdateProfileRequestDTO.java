package com.social.ProfileService.DTOs;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
	
	@Size(max = 100)
    private String displayName;
	
	@Size(max = 500)
    private String bio;

    private String profession;
    
    private String username;

    private String location;

    private String website;

    private String profilePictureUrl;

    private String coverPictureUrl;
}