package com.social.SocialGraphService.Exception;

public class SelfFollowNotAllowedException
extends RuntimeException {

public SelfFollowNotAllowedException(
    String message) {
super(message);
}
}
