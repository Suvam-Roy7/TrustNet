package com.social.SocialGraphService.Filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.social.SocialGraphService.Service.TokenBlacklistService;
import com.social.SocialGraphService.Utils.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	private final TokenBlacklistService tokenBlacklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.info(
		        "SocialGraph JWT filter: path={}, authorizationPresent={}",
		        request.getRequestURI(),
		        request.getHeader("Authorization") != null
		);

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		if (tokenBlacklistService.isBlacklisted(token)) {

			SecurityContextHolder.clearContext();

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			response.setContentType("application/json");

			response.getWriter().write("""
					{
					  "status": 401,
					  "error": "Unauthorized",
					  "message": "Token has been revoked"
					}
					""");

			return;
		}

		if (jwtUtil.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {

			Claims claims = jwtUtil.extractClaims(token);

			String userId = claims.getSubject();

			String role = claims.get("role", String.class);

			if (userId != null && role != null) {

				List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
						null, authorities);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		filterChain.doFilter(request, response);
	}
}