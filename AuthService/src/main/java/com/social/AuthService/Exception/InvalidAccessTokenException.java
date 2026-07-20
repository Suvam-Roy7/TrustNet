package com.social.AuthService.Exception;

public class InvalidAccessTokenException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidAccessTokenException(
            String message) {

        super(message);
    }
}