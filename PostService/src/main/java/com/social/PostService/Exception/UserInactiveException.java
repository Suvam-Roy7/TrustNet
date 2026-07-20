package com.social.PostService.Exception;

public class UserInactiveException
        extends RuntimeException {

    public UserInactiveException(String message) {
        super(message);
    }
}