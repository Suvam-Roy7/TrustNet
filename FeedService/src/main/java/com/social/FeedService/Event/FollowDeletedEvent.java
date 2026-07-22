package com.social.FeedService.Event;

import java.time.Instant;
import java.util.UUID;

public record FollowDeletedEvent(
        UUID eventId,
        UUID followerId,
        UUID followedUserId,
        Instant occurredAt
) {
}