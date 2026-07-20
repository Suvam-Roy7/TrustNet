package com.social.AuthService.Service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(
            String key,
            int maxRequests,
            Duration duration) {

        Long count =
                redisTemplate.opsForValue()
                        .increment(key);

        if (count != null && count == 1) {

            redisTemplate.expire(
                    key,
                    duration);
        }

        return count != null
                && count <= maxRequests;
    }
}