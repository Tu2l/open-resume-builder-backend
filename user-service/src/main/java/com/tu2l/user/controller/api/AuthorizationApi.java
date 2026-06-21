package com.tu2l.user.controller.api;

import com.tu2l.user.model.response.AuthorizationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tu2l.common.constant.CommonConstants;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Role-based access control (RBAC) endpoints. Mounted under {@code /v1/authorize}
 * (effective gateway path {@code /api/users/v1/authorize/**}); all routes require
 * a valid Bearer access token.
 */
@Tag(name = "Authorization", description = "Role-based access control (RBAC) — permission checks and role management")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/v1/authorize")
public interface AuthorizationApi {

    @Operation(
            summary = "Check permission",
            description = "Determines whether the caller (identified by the access token) may perform `action` on `resource`. " +
                          "Valid `resource` values: `user`, `role`, `pdf`. " +
                          "Valid `action` values: `read`, `write`, `delete`, `read_all`, `write_all`, `delete_all`, `generate`, `assign`. " +
                          "Inspect the `allowed` field in the response body for the result."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authorization check completed — see `allowed` in the response body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/check")
    ResponseEntity<AuthorizationResponse> checkPermission(
            @Parameter(description = "Resource to check (e.g. `user`, `pdf`, `role`)", required = true)
            @RequestParam String resource,
            @Parameter(description = "Action to check (e.g. `read`, `write`, `delete`)", required = true)
            @RequestParam String action,
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "List all roles",
            description = "Returns every role defined in the system. The `roles` field in the response contains the full set. **Requires ADMIN role.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved — `roles` field contains the set of role names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles")
    ResponseEntity<AuthorizationResponse> getAllRoles(
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "Get a user's role",
            description = "Returns the role currently assigned to the specified user. The `role` field in the response contains the role name. **Requires ADMIN role.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role retrieved — `role` field contains the role name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> getUserRoles(
            @Parameter(description = "ID of the target user", required = true)
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "Assign role to user",
            description = "Replaces the current role of the specified user. " +
                          "The request body is a plain-text role name. " +
                          "Valid values: `ADMIN`, `MODERATOR`, `USER`, `GUEST`. **Requires ADMIN role.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned — `role` field contains the newly assigned role name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unknown role name"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> assignRole(
            @Parameter(description = "ID of the target user", required = true)
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role name to assign — one of: `ADMIN`, `MODERATOR`, `USER`, `GUEST`",
                    required = true,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "USER",
                                    allowableValues = {"ADMIN", "MODERATOR", "USER", "GUEST"}))
            )
            @RequestBody String roleName,
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "List all permissions",
            description = "Returns every fine-grained permission defined in the system. " +
                          "The `permissions` field in the response contains the full set. **Requires ADMIN role.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved — `permissions` field contains the set of permission names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/permissions")
    ResponseEntity<AuthorizationResponse> getAllPermissions(
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "Get a user's permissions",
            description = "Returns the effective permissions for the specified user, derived from their assigned role. **Requires ADMIN role.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User permissions retrieved — `permissions` field contains the set",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/permissions/{userId}")
    ResponseEntity<AuthorizationResponse> getUserPermissions(
            @Parameter(description = "ID of the target user", required = true)
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);

    @Operation(
            summary = "Get my permissions",
            description = "Returns the effective permissions for the authenticated caller, derived from their current role. " +
                          "Available to any authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved — `permissions` field contains the set",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/me/permissions")
    ResponseEntity<AuthorizationResponse> getMyPermissions(
            @Parameter(hidden = true)
            @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole);
}
