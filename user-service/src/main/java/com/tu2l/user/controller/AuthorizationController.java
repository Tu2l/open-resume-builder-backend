package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.authorization.Permission;
import com.tu2l.user.controller.api.AuthorizationApi;
import com.tu2l.user.model.response.AuthorizationResponse;
import com.tu2l.user.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthorizationController implements AuthorizationApi {

    private final AuthorizationService authorizationService;

    @Override
    public ResponseEntity<AuthorizationResponse> checkPermission(String resource, String action, String userRole) {
        log.info("Checking permission - resource: {}, action: {}", resource, action);
        UserRole role = parseRole(userRole);
        boolean allowed = authorizationService.hasPermission(role, resource, action);
        return ok(AuthorizationResponse.builder().allowed(allowed).build(), "Authorization check completed");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getAllRoles(String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        Set<String> roles = authorizationService.getAllRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return ok(AuthorizationResponse.builder().roles(roles).build(), "Roles retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getUserRoles(Long userId, String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        UserRole role = authorizationService.getUserRole(userId);
        return ok(AuthorizationResponse.builder().role(role.name()).build(), "User role retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> assignRole(Long userId, String roleName, String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        UserRole role = authorizationService.assignRole(userId, roleName);
        return ok(AuthorizationResponse.builder().role(role.name()).build(), "Role assigned successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getAllPermissions(String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        return ok(AuthorizationResponse.builder().permissions(toNames(authorizationService.getAllPermissions())).build(),
                "Permissions retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getUserPermissions(Long userId, String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        return ok(AuthorizationResponse.builder().permissions(toNames(authorizationService.getPermissionsForUser(userId))).build(),
                "User permissions retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getMyPermissions(String userRole) {
        UserRole role = parseRole(userRole);
        return ok(AuthorizationResponse.builder().permissions(toNames(authorizationService.getPermissionsForRole(role))).build(),
                "Permissions retrieved successfully");
    }

    // --- helpers ---

    private boolean isAdmin(String userRole) {
        return UserRole.ADMIN.name().equals(userRole);
    }

    private UserRole parseRole(String userRole) {
        try {
            return UserRole.valueOf(userRole);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role from gateway header: {}", userRole);
            return UserRole.GUEST;
        }
    }

    private Set<String> toNames(Set<Permission> permissions) {
        return permissions.stream().map(Enum::name).collect(Collectors.toSet());
    }

    private ResponseEntity<AuthorizationResponse> ok(AuthorizationResponse response, String message) {
        return ResponseEntity.ok(ResponseFactory.configureResponse(response, message, ResponseProcessingStatus.SUCCESS));
    }

    private ResponseEntity<AuthorizationResponse> forbidden() {
        log.warn("Unauthorized access attempt to an admin-only authorization endpoint");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
