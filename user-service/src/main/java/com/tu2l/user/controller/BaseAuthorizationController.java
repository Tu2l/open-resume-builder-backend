package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.user.model.response.AuthorizationResponse;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared helpers for authorization controllers.
 * Eliminates duplicate {@code ok()}/{@code toNames()} across
 * {@link AuthorizationController} and {@link AdminAuthorizationController}.
 */
abstract class BaseAuthorizationController extends BaseController {

    protected ResponseEntity<AuthorizationResponse> ok(AuthorizationResponse response, String message) {
        return ResponseEntity.ok(ResponseFactory.configureResponse(response, message, ResponseProcessingStatus.SUCCESS));
    }

    protected <E extends Enum<E>> Set<String> toNames(Set<E> enums) {
        return enums.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
