package com.social.APIGateway.Filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.social.APIGateway.Service.TokenBlacklistService;
import com.social.APIGateway.Utils.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

	private static final String USER_ID_HEADER = "X-User-Id";

	private final JwtUtil jwtUtil;

	private final TokenBlacklistService tokenBlacklistService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {

		super(Config.class);

		this.jwtUtil = jwtUtil;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			String path = exchange.getRequest().getURI().getPath();

			HttpMethod method = exchange.getRequest().getMethod();

			String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			log.info("Gateway request: method={}, path={}, authorizationPresent={}", method, path,
					authorizationHeader != null);

			/*
			 * Allow browser CORS preflight requests.
			 */
			if (HttpMethod.OPTIONS.equals(method)) {

				log.debug("Allowing OPTIONS request for path={}", path);

				return chain.filter(exchange);
			}

			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

				log.warn("Authorization header missing for path={}", path);

				return unauthorized(exchange, "Authorization token is missing");
			}

			String token = authorizationHeader.substring(7).trim();

			if (token.isBlank()) {

				log.warn("Bearer token is empty for path={}", path);

				return unauthorized(exchange, "Authorization token is missing");
			}

			return Mono.defer(() -> {

				boolean validToken = jwtUtil.validateToken(token);

				if (!validToken) {

					log.warn("JWT validation returned false for path={}", path);

					return unauthorized(exchange, "Invalid or expired token");
				}

				Claims claims = jwtUtil.extractClaims(token);

				String userId = claims.getSubject();

				log.info("JWT validated for path={}, subject={}", path, userId);

				if (userId == null || userId.isBlank()) {

					log.warn("JWT subject is missing for path={}", path);

					return unauthorized(exchange, "User ID is missing from token");
				}

				return tokenBlacklistService.isBlacklisted(token).defaultIfEmpty(false).flatMap(isBlacklisted -> {

					if (Boolean.TRUE.equals(isBlacklisted)) {

						log.warn("Blacklisted JWT rejected for path={}, userId={}", path, userId);

						return unauthorized(exchange, "Token has been revoked");
					}

					/*
					 * Remove any browser-supplied X-User-Id and replace it using the validated JWT
					 * subject.
					 */
					ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().headers(headers -> {

						headers.remove(USER_ID_HEADER);

						headers.set(USER_ID_HEADER, userId);
					}).build();

					ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

					log.info("Forwarding authenticated request: path={}, userId={}", path, userId);

					return chain.filter(mutatedExchange);
				});

			}).onErrorResume(exception -> {

				log.error("Gateway authentication failed: path={}, type={}, message={}", path,
						exception.getClass().getSimpleName(), exception.getMessage(), exception);

				return unauthorized(exchange, "Invalid authentication token");
			});
		};
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {

		if (exchange.getResponse().isCommitted()) {
			return Mono.empty();
		}

		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String responseBody = """
				{
				  "status": 401,
				  "error": "Unauthorized",
				  "message": "%s"
				}
				""".formatted(escapeJson(message));

		byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

		exchange.getResponse().getHeaders().setContentLength(bytes.length);

		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

	private String escapeJson(String value) {

		if (value == null) {
			return "";
		}

		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	public static class Config {
	}
}