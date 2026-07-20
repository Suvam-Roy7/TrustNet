package com.social.MediaService.Service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.social.MediaService.DTOs.UploadResponseDTO;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

	private final MinioClient minioClient;

	@Value("${minio.bucket-name}")
	private String bucketName;

	@Value("${minio.url}")
	private String minioUrl;

	@Override
	public UploadResponseDTO uploadFile(String userId, MultipartFile file) {

		try {

			String uniqueFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

			String objectName = "users/" + userId + "/images/" + uniqueFileName;

			minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName)
					.stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());

			String fileUrl = minioUrl + "/" + bucketName + "/" + objectName;

			return UploadResponseDTO.builder().fileUrl(fileUrl).fileName(uniqueFileName).objectName(objectName)
					.size(file.getSize()).build();

		} catch (Exception ex) {

			throw new RuntimeException("File upload failed", ex);
		}
	}

	@Override
	public void deleteFile(String userId, String objectName) {

		try {

			String expectedPrefix = "users/" + userId + "/";

			if (!objectName.startsWith(expectedPrefix)) {

				throw new RuntimeException("You are not authorized to delete this file");
			}

			minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());

		} catch (RuntimeException ex) {

		    throw ex;
		}
		catch (Exception ex) {

		    throw new RuntimeException(
		            "File deletion failed",
		            ex);
		}
	}
	
	private String getLoggedInUserId() {

	    Authentication authentication =
	            SecurityContextHolder
	                    .getContext()
	                    .getAuthentication();

	    return authentication.getName();
	}
}