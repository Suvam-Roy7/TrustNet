package com.social.AuthService.Exception;

public class UserAccountSuspendedException
extends RuntimeException {

private static final long serialVersionUID = 1L;

public UserAccountSuspendedException(String message) {
super(message);
}
}
