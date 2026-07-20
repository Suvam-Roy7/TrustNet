package com.social.PostService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.PostHashtag;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, UUID> {

	List<PostHashtag> findByPostId(UUID postId);

	void deleteByPostId(UUID postId);

}
