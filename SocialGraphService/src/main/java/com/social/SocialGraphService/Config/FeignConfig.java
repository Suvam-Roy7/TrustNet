package com.social.SocialGraphService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor jwtInterceptor() {

        return template -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder
                                    .getRequestAttributes();

            if (attributes != null) {

                String authHeader =
                        attributes
                                .getRequest()
                                .getHeader("Authorization");

                if (authHeader != null) {

                    template.header(
                            "Authorization",
                            authHeader);
                }
            }
        };
    }
}