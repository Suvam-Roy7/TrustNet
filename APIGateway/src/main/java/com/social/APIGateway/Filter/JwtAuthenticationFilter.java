package com.social.APIGateway.Filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.social.APIGateway.Service.TokenBlacklistService;
import com.social.APIGateway.Utils.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

	private final JwtUtil jwtUtil;

	private final TokenBlacklistService tokenBlacklistService;

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {

				return onError(exchange, "Missing token", HttpStatus.UNAUTHORIZED);
			}

			String token = authHeader.substring(7);

			if (!jwtUtil.validateToken(token)) {

				return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
			}

			return tokenBlacklistService.isBlacklisted(token).flatMap(blacklisted -> {

				if (blacklisted) {

					return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
				}

				return chain.filter(exchange);
			});
		};
	}

	public static class Config {
	}

	private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {

		exchange.getResponse().setStatusCode(status);

		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		byte[] bytes = ("""
				{
				  "status": %d,
				  "error": "%s",
				  "message": "%s"
				}
				""").formatted(status.value(), status.getReasonPhrase(), message).getBytes(StandardCharsets.UTF_8);

		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

		return exchange.getResponse().writeWith(Mono.just(buffer));
	}
}