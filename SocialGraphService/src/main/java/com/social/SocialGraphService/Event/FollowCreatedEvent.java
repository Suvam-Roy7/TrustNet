package com.social.SocialGraphService.Event;

import java.time.Instant;
import java.util.UUID;

public record FollowCreatedEvent(

        UUID eventId,

        UUID followerId,

        UUID followedUserId,

        Instant occurredAt) {
}