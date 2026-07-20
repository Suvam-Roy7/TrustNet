package com.social.SocialGraphService.Exception;

public class FollowAlreadyExistsException
        extends RuntimeException {

    public FollowAlreadyExistsException(
            String message) {
        super(message);
    }
}