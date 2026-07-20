package com.social.PostService.DTOs;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequestDTO {

    @NotBlank
    private String content;
    
    private List<MediaAttachmentDTO> attachments;
}
