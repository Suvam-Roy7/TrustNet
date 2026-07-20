package com.social.NotificationService.Exception;

public class NotificationNotFoundException
        extends RuntimeException {

    public NotificationNotFoundException(
            String message) {
        super(message);
    }
}
