package com.social.MediaService.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.social.MediaService.DTOs.UploadResponseDTO;
import com.social.MediaService.Service.MediaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

	private final MediaService service;

	@PostMapping("/upload")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UploadResponseDTO> uploadFile(

			@RequestHeader("X-User-Id") String userId,

			@RequestParam("file") MultipartFile file) {

		return ResponseEntity.ok(service.uploadFile(userId, file));
	}

	@DeleteMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> deleteFile(

			@RequestHeader("X-User-Id") String userId,

			@RequestParam String objectName) {

		service.deleteFile(userId, objectName);

		return ResponseEntity.ok("File deleted");
	}
}