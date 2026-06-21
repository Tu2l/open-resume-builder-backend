package com.tu2l.user.controller.api;

import com.tu2l.user.model.response.AuthorizationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Role-based access control (RBAC) endpoints. Mounted under {@code /v1/authorize}
 * (effective gateway path {@code /api/users/v1/authorize/**}); all routes are
 * protected (require a valid access token).
 */
@RequestMapping("/v1/authorize")
public interface AuthorizationApi {

    /** Check whether the caller may perform {@code action} on {@code resource}. */
    @GetMapping("/check")
    ResponseEntity<AuthorizationResponse> checkPermission(
            @RequestParam String resource,
            @RequestParam String action,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** List all roles available in the system (admin only). */
    @GetMapping("/roles")
    ResponseEntity<AuthorizationResponse> getAllRoles(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** Get the role assigned to a specific user (admin only). */
    @GetMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> getUserRoles(
            @PathVariable Long userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** Assign a role to a specific user (admin only). */
    @PostMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> assignRole(
            @PathVariable Long userId,
            @RequestBody String roleName,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** List all permissions available in the system (admin only). */
    @GetMapping("/permissions")
    ResponseEntity<AuthorizationResponse> getAllPermissions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** Get the permissions for a specific user (admin only). */
    @GetMapping("/permissions/{userId}")
    ResponseEntity<AuthorizationResponse> getUserPermissions(
            @PathVariable Long userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);

    /** Get the current caller's permissions. */
    @GetMapping("/me/permissions")
    ResponseEntity<AuthorizationResponse> getMyPermissions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);
}
