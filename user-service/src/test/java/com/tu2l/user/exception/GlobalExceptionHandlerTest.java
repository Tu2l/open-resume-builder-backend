package com.tu2l.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void plainUserException_mapsToBadRequest() {
        var response = handler.handleUserException(new UserException("invalid"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void userNotFound_mapsToNotFound() {
        var response = handler.handleUserException(new UserNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateUser_mapsToConflict() {
        var response = handler.handleUserException(new DuplicateUserException("exists"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
