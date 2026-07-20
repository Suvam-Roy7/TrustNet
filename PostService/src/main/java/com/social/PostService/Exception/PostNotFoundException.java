package com.social.PostService.Exception;

public class PostNotFoundException
        extends RuntimeException {

    public PostNotFoundException(String message) {
        super(message);
    }
}
