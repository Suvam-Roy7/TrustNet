package com.social.SocialGraphService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    private static final String USER_ID_HEADER =
            "X-User-Id";

    @Bean
    public RequestInterceptor requestInterceptor() {

        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder
                                    .getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest incomingRequest =
                    attributes.getRequest();

            String authorization =
                    incomingRequest.getHeader(
                            HttpHeaders.AUTHORIZATION
                    );

            String userId =
                    incomingRequest.getHeader(
                            USER_ID_HEADER
                    );

            if (authorization != null
                    && !authorization.isBlank()) {

                requestTemplate.header(
                        HttpHeaders.AUTHORIZATION,
                        authorization
                );
            }

            if (userId != null
                    && !userId.isBlank()) {

                requestTemplate.header(
                        USER_ID_HEADER,
                        userId
                );
            }
        };
    }
}