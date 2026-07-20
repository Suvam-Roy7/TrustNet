package com.social.NotificationService.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleNotificationNotFound(
            NotificationNotFoundException ex) {

        return new ResponseEntity<>(

                new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()),

                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleException(
            Exception ex) {

        return new ResponseEntity<>(

                new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()),

                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
