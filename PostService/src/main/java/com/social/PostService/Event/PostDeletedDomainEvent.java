package com.social.PostService.Event;

import java.time.Instant;
import java.util.UUID;

public record PostDeletedDomainEvent(
        UUID eventId,
        UUID postId,
        UUID authorId,
        Instant occurredAt) {
}
