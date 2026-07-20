package com.social.PostService.DTOs;

import com.social.PostService.Entity.MediaType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaResponseDTO {

    private String fileUrl;

    private MediaType mediaType;
}