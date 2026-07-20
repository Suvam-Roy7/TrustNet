package com.social.PostService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.PostService.Entity.Mention;

public interface MentionRepository
extends JpaRepository<Mention, UUID> {

Optional<Mention>
findByUsername(String username);
}
