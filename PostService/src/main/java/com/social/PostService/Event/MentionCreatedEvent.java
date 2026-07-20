package com.social.PostService.Event;

import java.time.Instant;
import java.util.UUID;

public record MentionCreatedEvent(

        UUID eventId,

        UUID postId,

        UUID actorUserId,

        UUID recipientUserId,

        String actorUsername,

        Instant occurredAt) {
}