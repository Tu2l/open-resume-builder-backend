package com.tu2l.user.exception;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull BaseResponse> handleGlobalExceptions(Exception exception) {
        log.error("Exception caught", exception);
        return getResponse("Something went wrong", "Exception caught: {}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<@NonNull BaseResponse> handleResponseStatusException(ResponseStatusException exception) {
        // Honour the status carried by the exception (e.g. Spring's native API-versioning errors
        // InvalidApiVersionException/NotAcceptableApiVersionException -> 400) instead of letting the
        // generic Exception handler mask it as 500.
        String message = exception.getReason() != null ? exception.getReason() : exception.getMessage();
        log.warn("ResponseStatusException caught: {}", message);
        return new ResponseEntity<>(ResponseFactory.createErrorResponse(message), exception.getStatusCode());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<@NonNull BaseResponse> handleBadRequests(HttpMessageConversionException exception) {
        log.error("Exception caught", exception);
        return getResponse("Something went wrong", "Exception caught: {}", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<@NonNull BaseResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return getResponse("Resource not found", "Resource not found: {}", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull BaseResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return getResponse(errorMessage, "Validation failed: {}", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserException.class})
    public ResponseEntity<@NonNull BaseResponse> handleUserException(UserException exception) {
        return getResponse(exception.getMessage(), "UserException caught: {}", exception.getStatus());
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<@NonNull BaseResponse> handleAuthenticationException(AuthenticationException exception) {
        return getResponse(exception.getMessage(), "AuthenticationException caught: {}", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({JwtException.class})
    public ResponseEntity<@NonNull BaseResponse> handleJwtException(JwtException exception) {
        return getResponse(exception.getMessage(), "JwtException caught: {}", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<@NonNull BaseResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return getResponse(exception.getMessage(), "HttpRequestMethodNotSupportedException caught: {}", HttpStatus.METHOD_NOT_ALLOWED);
    }

    private ResponseEntity<@NonNull BaseResponse> getResponse(String message, String format, HttpStatus ok) {
        log.warn(format, message);
        return new ResponseEntity<>(ResponseFactory.createErrorResponse(message), ok);
    }
}