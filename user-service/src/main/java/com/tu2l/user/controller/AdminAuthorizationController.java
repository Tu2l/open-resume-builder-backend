package com.tu2l.user.controller;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.controller.api.AdminAuthorizationApi;
import com.tu2l.user.model.response.AuthorizationResponse;
import com.tu2l.user.service.AdminAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminAuthorizationController extends BaseAuthorizationController implements AdminAuthorizationApi {

    private final AdminAuthorizationService authorizationService;

    @Override
    public ResponseEntity<AuthorizationResponse> getAllRoles(String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        return ok(AuthorizationResponse.builder()
                .roles(toNames(authorizationService.getAllRoles())).build(), "Roles retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getUserRole(Long userId, String userRole) {
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
        return ok(AuthorizationResponse.builder()
                .permissions(toNames(authorizationService.getAllPermissions())).build(),
                "Permissions retrieved successfully");
    }

    @Override
    public ResponseEntity<AuthorizationResponse> getUserPermissions(Long userId, String userRole) {
        if (!isAdmin(userRole)) return forbidden();
        return ok(AuthorizationResponse.builder()
                .permissions(toNames(authorizationService.getPermissionsForUser(userId))).build(),
                "User permissions retrieved successfully");
    }
}
