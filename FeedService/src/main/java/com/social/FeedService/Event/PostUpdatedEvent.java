package com.social.FeedService.Event;

import java.time.Instant;
import java.util.UUID;

public record PostUpdatedEvent(
        UUID eventId,
        UUID postId,
        UUID authorId,
        String content,
        Instant updatedAt) {
}