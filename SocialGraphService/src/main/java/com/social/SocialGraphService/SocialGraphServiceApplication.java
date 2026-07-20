package com.social.SocialGraphService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.social.SocialGraphService.Config.FeignConfig;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignConfig.class)
@EnableMethodSecurity
public class SocialGraphServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialGraphServiceApplication.class, args);
	}

}
