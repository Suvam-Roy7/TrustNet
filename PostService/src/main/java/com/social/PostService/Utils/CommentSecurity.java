package com.social.PostService.Utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.social.PostService.Entity.Comment;
import com.social.PostService.Repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Component("commentSecurity")
@RequiredArgsConstructor
public class CommentSecurity {

    private final CommentRepository repository;

    public boolean isOwner(
            UUID commentId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String userId =
                authentication.getName();

        return repository.findById(commentId)
                .map(Comment::getUserId)
                .map(ownerId ->
                        ownerId.equals(
                                UUID.fromString(userId)))
                .orElse(false);
    }
}