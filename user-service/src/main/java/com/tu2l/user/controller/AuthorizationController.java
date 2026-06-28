package com.tu2l.user.controller;

import com.tu2l.user.controller.api.AuthorizationApi;
import com.tu2l.user.model.response.AuthorizationResponse;
import com.tu2l.user.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthorizationController extends BaseAuthorizationController implements AuthorizationApi {

    private final AuthorizationService authorizationService;

    @Override
    public ResponseEntity<AuthorizationResponse> checkPermission(String resource, String action) {
        log.info("Checking permission - resource: {}, action: {}", resource, action);
        boolean allowed = authorizationService.hasPermission(getCurrentUserRole(), resource, action);
        return ok(AuthorizationResponse.builder().allowed(allowed).build(), "Authorization check completed");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getMyPermissions() {
        return ok(AuthorizationResponse.builder()
                        .permissions(toNames(authorizationService.getPermissionsForRole(getCurrentUserRole()))).build(),
                "Permissions retrieved successfully");
    }
}
