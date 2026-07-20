package com.social.PostService.DTOs;

import com.social.PostService.Entity.MediaType;

import lombok.Data;

@Data
public class MediaAttachmentDTO {

    private String fileUrl;

    private MediaType mediaType;
    
    private String objectName;
}
