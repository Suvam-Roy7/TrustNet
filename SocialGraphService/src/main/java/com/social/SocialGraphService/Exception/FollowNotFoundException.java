package com.social.SocialGraphService.Exception;

public class FollowNotFoundException
        extends RuntimeException {

    public FollowNotFoundException(
            String message) {
        super(message);
    }
}