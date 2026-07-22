package com.social.AuthService.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI authServiceOpenAPI() {

		Server gatewayServer = new Server().url("http://localhost:7000").description("TrustNet API Gateway");

		return new OpenAPI()
				.info(new Info().title("TrustNet Auth Service API")
						.description("Authentication and user management APIs").version("v1"))
				.servers(List.of(gatewayServer));
	}
}