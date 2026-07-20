package com.social.AuthService.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.social.AuthService.Entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "TrustNetSecretKeyForJWTAuthentication123456789";

    private final Key key =
            Keys.hmacShaKeyFor(
                    SECRET_KEY.getBytes());

    public String generateToken(UUID userId, String email, Role role) {

        return Jwts.builder()
        		.subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }
    
    public boolean validateToken(String token) {

        try {

            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }
    
    public Date extractExpiration(String token) {

        return extractClaims(token).getExpiration();
    }
    
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
