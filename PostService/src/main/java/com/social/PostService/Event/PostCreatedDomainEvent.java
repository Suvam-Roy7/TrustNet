package com.social.PostService.Event;

import java.time.Instant;
import java.util.UUID;

public record PostCreatedDomainEvent(

        UUID eventId,

        UUID postId,

        UUID authorId,

        String content,

        Instant createdAt) {
}