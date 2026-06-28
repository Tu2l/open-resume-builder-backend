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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only RBAC management endpoints. Mounted under {@code /admin/authorize} with API version
 * {@code 1+} resolved from the {@code /v1} path segment (effective gateway path
 * {@code /api/users/v1/admin/authorize/**}); all routes require ADMIN role.
 * Self-service authorization queries live in {@link AuthorizationApi}.
 */
@Tag(name = "Admin — Authorization", description = "Admin-only RBAC management operations")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/admin/authorize", version = "1+")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminAuthorizationApi {

    @Operation(summary = "List all roles", description = "Returns every role defined in the system. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role")
    })
    @GetMapping("/roles")
    ResponseEntity<AuthorizationResponse> getAllRoles();

    @Operation(summary = "Get a user's role", description = "Returns the role assigned to the specified user. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> getUserRole(
            @Parameter(description = "ID of the user whose role to retrieve", required = true) @PathVariable("userId") Long userId);

    @Operation(summary = "Assign role to user",
            description = "Replaces the user's current role. Valid values: `ADMIN`, `MODERATOR`, `USER`, `GUEST`. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unknown role name"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/roles/{userId}")
    ResponseEntity<AuthorizationResponse> assignRole(
            @Parameter(description = "ID of the user to assign the role to", required = true) @PathVariable("userId") Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role name — one of: `ADMIN`, `MODERATOR`, `USER`, `GUEST`", required = true,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "USER",
                                    allowableValues = {"ADMIN", "MODERATOR", "USER", "GUEST"})))
            @RequestBody String roleName);

    @Operation(summary = "List all permissions",
            description = "Returns every fine-grained permission defined in the system. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role")
    })
    @GetMapping("/permissions")
    ResponseEntity<AuthorizationResponse> getAllPermissions();

    @Operation(summary = "Get a user's permissions",
            description = "Returns the effective permissions for the specified user, derived from their role. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User permissions retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/permissions/{userId}")
    ResponseEntity<AuthorizationResponse> getUserPermissions(
            @Parameter(description = "ID of the user whose permissions to retrieve", required = true) @PathVariable("userId") Long userId);
}
