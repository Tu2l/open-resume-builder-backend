package com.tu2l.user.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested user cannot be found. Surfaces as HTTP 404.
 */
public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
