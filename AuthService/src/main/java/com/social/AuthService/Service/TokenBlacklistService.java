package com.social.AuthService.Service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private static final String PREFIX = "blacklist:";

	private final StringRedisTemplate redisTemplate;

	public void blacklistToken(String token, long ttlMillis) {

		if (ttlMillis <= 0) {
			return;
		}

		redisTemplate.opsForValue().set(PREFIX + token, "true", Duration.ofMillis(ttlMillis));
	}

	public boolean isBlacklisted(String token) {

		return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
	}
}