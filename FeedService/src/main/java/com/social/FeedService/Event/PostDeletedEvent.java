package com.social.FeedService.Event;

import java.time.Instant;
import java.util.UUID;

public record PostDeletedEvent(
        UUID eventId,
        UUID postId,
        UUID authorId,
        Instant occurredAt) {
}