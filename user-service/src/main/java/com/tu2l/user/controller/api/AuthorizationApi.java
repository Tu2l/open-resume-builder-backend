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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Self-service authorization endpoints — available to any authenticated user.
 * Admin RBAC management lives in {@link com.tu2l.user.controller.AdminAuthorizationController}.
 */
@Tag(name = "Authorization", description = "Permission checks and self-service RBAC queries")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/authorize", version = "1+")
public interface AuthorizationApi {

    @Operation(
            summary = "Check permission",
            description = "Determines whether the caller may perform `action` on `resource`. " +
                    "Valid `resource` values: `user`, `role`, `pdf`. " +
                    "Valid `action` values: `read`, `write`, `delete`, `read_all`, `write_all`, `delete_all`, `generate`, `assign`. " +
                    "Inspect the `allowed` field in the response body for the result."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed — see `allowed` in the response body",
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
            @RequestParam String action);

    @Operation(
            summary = "Get my permissions",
            description = "Returns the effective permissions for the authenticated caller, derived from their current role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved — `permissions` field contains the set",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/me/permissions")
    ResponseEntity<AuthorizationResponse> getMyPermissions();
}
