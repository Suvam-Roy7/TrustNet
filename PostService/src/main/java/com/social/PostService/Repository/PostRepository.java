package com.social.PostService.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

	Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	List<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
