package com.tu2l.user.exception;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull BaseResponse> handleGlobalExceptions(Exception exception) {
        log.error("Exception caught", exception);
        return getResponse(exception.getMessage(), "Exception caught: {}", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<@NonNull BaseResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return getResponse(exception.getMessage(), "Resource not found: {}", HttpStatus.NOT_FOUND);
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
        return getResponse(exception.getMessage(), "UserException caught: {}", HttpStatus.OK);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<@NonNull BaseResponse> handleAuthenticationException(AuthenticationException exception) {
        return getResponse(exception.getMessage(), "AuthenticationException caught: {}", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({JwtException.class})
    public ResponseEntity<@NonNull BaseResponse> handleJwtException(JwtException exception) {
        return getResponse(exception.getMessage(), "JwtException caught: {}", HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<@NonNull BaseResponse> getResponse(String message, String format, HttpStatus ok) {
        log.warn(format, message);
        return new ResponseEntity<>(ResponseFactory.createErrorResponse(message), ok);
    }
}