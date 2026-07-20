package com.social.MediaService.Service;

import org.springframework.web.multipart.MultipartFile;

import com.social.MediaService.DTOs.UploadResponseDTO;

public interface MediaService {

	UploadResponseDTO uploadFile(String userId, 
            MultipartFile file);
    
    void deleteFile(String userId, String objectName);
}