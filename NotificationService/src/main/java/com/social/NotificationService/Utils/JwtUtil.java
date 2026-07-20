package com.social.NotificationService.Utils;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final String SECRET = "TrustNetSecretKeyForJWTAuthentication123456789";

	private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

	public Claims extractClaims(String token) {

		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public boolean validateToken(String token) {

		try {

			extractClaims(token);

			return true;

		} catch (Exception ex) {

			return false;
		}
	}
}
