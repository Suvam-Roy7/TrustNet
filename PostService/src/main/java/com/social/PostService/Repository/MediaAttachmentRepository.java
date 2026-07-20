package com.social.PostService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.MediaAttachment;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, UUID> {

	List<MediaAttachment> findByPostId(UUID postId);

	void deleteByPostId(UUID postId);
}
