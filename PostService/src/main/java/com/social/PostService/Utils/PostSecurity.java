package com.social.PostService.Utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.social.PostService.Entity.Post;
import com.social.PostService.Repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Component("postSecurity")
@RequiredArgsConstructor
public class PostSecurity {

    private final PostRepository repository;

    public boolean isOwner(UUID postId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String userId =
                authentication.getName();

        return repository.findById(postId)
                .map(Post::getUserId)
                .map(ownerId ->
                        ownerId.equals(
                                UUID.fromString(userId)))
                .orElse(false);
    }
}