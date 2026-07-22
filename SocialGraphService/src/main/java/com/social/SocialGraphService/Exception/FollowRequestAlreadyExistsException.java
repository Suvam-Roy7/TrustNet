package com.social.SocialGraphService.Exception;

public class FollowRequestAlreadyExistsException
        extends RuntimeException {

    public FollowRequestAlreadyExistsException(
            String message) {

        super(message);
    }
}