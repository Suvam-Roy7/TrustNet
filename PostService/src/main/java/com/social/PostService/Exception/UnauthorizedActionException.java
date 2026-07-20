package com.social.PostService.Exception;

public class UnauthorizedActionException
extends RuntimeException {

public UnauthorizedActionException(
    String message) {

super(message);
}
}
