package com.social.PostService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.Hashtag;

public interface HashtagRepository
extends JpaRepository<Hashtag, UUID> {

Optional<Hashtag> findByTag(String tag);
}
