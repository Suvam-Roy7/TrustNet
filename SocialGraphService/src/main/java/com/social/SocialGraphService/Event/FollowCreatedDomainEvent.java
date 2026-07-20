package com.social.SocialGraphService.Event;

import java.time.Instant;
import java.util.UUID;

public record FollowCreatedDomainEvent(

        UUID eventId,

        UUID followerId,

        UUID followedUserId,

        Instant occurredAt) {
}