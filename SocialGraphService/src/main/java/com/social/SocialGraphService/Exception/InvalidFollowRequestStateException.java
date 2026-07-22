package com.social.SocialGraphService.Exception;

public class InvalidFollowRequestStateException
        extends RuntimeException {

    public InvalidFollowRequestStateException(
            String message) {

        super(message);
    }
}