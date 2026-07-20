package com.social.AuthService.Exception;

public class RateLimitExceededException
        extends RuntimeException {

    public RateLimitExceededException(
            String message) {

        super(message);
    }
}