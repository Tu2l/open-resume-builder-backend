package com.tu2l.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserException extends RuntimeException {

    private final HttpStatus status;

    public UserException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
