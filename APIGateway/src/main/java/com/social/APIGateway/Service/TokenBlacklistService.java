package com.social.APIGateway.Service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isBlacklisted(String token) {

        return redisTemplate.hasKey(
                PREFIX + token);
    }
}