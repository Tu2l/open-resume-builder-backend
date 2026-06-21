package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Root base class for all controllers in user-service.
 * Provides: admin role guard, typed 403 helper, success/error response builders.
 */
abstract class BaseController {

    protected boolean isAdmin(String role) {
        return UserRole.ADMIN.name().equals(role);
    }

    protected <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    protected ResponseEntity<BaseResponse> success(String message) {
        return ResponseEntity.ok(ResponseFactory.createSuccessResponse(message));
    }

    protected ResponseEntity<BaseResponse> getResponse(boolean succeeded, String successMessage, String failureMessage) {
        if (!succeeded) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseFactory.createErrorResponse(failureMessage));
        }
        return ResponseEntity.ok(ResponseFactory.createSuccessResponse(successMessage));
    }
}
