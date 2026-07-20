package com.social.PostService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.PostMention;

public interface PostMentionRepository extends JpaRepository<PostMention, UUID> {

	List<PostMention> findByPostId(UUID postId);

	void deleteByPostId(UUID postId);
}
