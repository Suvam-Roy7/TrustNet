package com.social.AuthService.Exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.social.AuthService.DTOs.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {

		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {

		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()),
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {

		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
				HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {

		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

		String error = ex.getBindingResult().getFieldError().getDefaultMessage();

		return new ResponseEntity<>(new ErrorResponse(error, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UserAccountSuspendedException.class)
	public ResponseEntity<ErrorResponseDTO> handleSuspendedUser(UserAccountSuspendedException ex,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponseDTO.builder().timestamp(LocalDateTime.now()).status(HttpStatus.FORBIDDEN.value())
						.error(HttpStatus.FORBIDDEN.getReasonPhrase()).message(ex.getMessage())
						.path(request.getRequestURI()).build());
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<ErrorResponseDTO> handleInvalidRefreshToken(InvalidRefreshTokenException ex,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ErrorResponseDTO.builder().timestamp(LocalDateTime.now()).status(HttpStatus.UNAUTHORIZED.value())
						.error(HttpStatus.UNAUTHORIZED.getReasonPhrase()).message(ex.getMessage())
						.path(request.getRequestURI()).build());
	}

	@ExceptionHandler(InvalidAccessTokenException.class)
	public ResponseEntity<ErrorResponseDTO> handleInvalidAccessToken(InvalidAccessTokenException ex,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ErrorResponseDTO.builder().timestamp(LocalDateTime.now()).status(HttpStatus.UNAUTHORIZED.value())
						.error(HttpStatus.UNAUTHORIZED.getReasonPhrase()).message(ex.getMessage())
						.path(request.getRequestURI()).build());
	}
}
