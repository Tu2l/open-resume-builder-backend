package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.base.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Root base class for all controllers in user-service.
 * Provides: success/error response builders and pagination helper.
 * Admin role guards are enforced declaratively via {@code @PreAuthorize("hasRole('ADMIN')")}
 * on individual controller methods — no manual {@code isAdmin()} check needed.
 */
abstract class BaseController {

    /** Maps a Spring Data {@link Page} into a transport-stable {@link PagedResponse}. */
    protected <S, T> PagedResponse<T> paged(Page<S> page, Function<? super S, ? extends T> mapper) {
        Page<T> mapped = page.map(mapper);
        return PagedResponse.of(mapped.getContent(), mapped.getNumber(), mapped.getSize(),
                mapped.getTotalElements(), mapped.getTotalPages());
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
