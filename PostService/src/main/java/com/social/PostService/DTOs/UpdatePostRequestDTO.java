package com.social.PostService.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePostRequestDTO {

    @NotBlank
    private String content;
}
