package com.tu2l.user.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when registering a user that conflicts with an existing username/email.
 * Surfaces as HTTP 409.
 */
public class DuplicateUserException extends UserException {
    public DuplicateUserException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
