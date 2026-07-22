package com.social.ProfileService.Filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.social.ProfileService.Service.TokenBlacklistService;
import com.social.ProfileService.Utils.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final TokenBlacklistService tokenBlacklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		/*
		 * No token means this filter does not authenticate the request. SecurityConfig
		 * will decide whether the endpoint is public.
		 */
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7).trim();

		if (token.isBlank()) {
			sendUnauthorized(response, "Authorization token is missing");
			return;
		}

		try {
			/*
			 * Validate the JWT before reading its claims.
			 */
			if (!jwtUtil.validateToken(token)) {
				sendUnauthorized(response, "Invalid or expired token");
				return;
			}

			/*
			 * Reject tokens revoked during logout.
			 */
			if (tokenBlacklistService.isBlacklisted(token)) {
				SecurityContextHolder.clearContext();

				sendUnauthorized(response, "Token has been revoked");
				return;
			}

			if (SecurityContextHolder.getContext().getAuthentication() == null) {

				Claims claims = jwtUtil.extractClaims(token);

				String userId = claims.getSubject();

				String role = claims.get("role", String.class);

				if (userId == null || userId.isBlank()) {
					sendUnauthorized(response, "User ID is missing from token");
					return;
				}

				if (role == null || role.isBlank()) {
					sendUnauthorized(response, "User role is missing from token");
					return;
				}

				String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

				List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
						null, authorities);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);

		} catch (Exception exception) {
			SecurityContextHolder.clearContext();

			sendUnauthorized(response, "Invalid or expired token");
		}
	}

	private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		response.setContentType("application/json");

		response.setCharacterEncoding("UTF-8");

		response.getWriter().write("""
				{
				  "status": 401,
				  "error": "Unauthorized",
				  "message": "%s"
				}
				""".formatted(message));
	}
}