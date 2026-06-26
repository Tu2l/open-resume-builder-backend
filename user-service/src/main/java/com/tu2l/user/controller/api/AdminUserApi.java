package com.tu2l.user.controller.api;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.base.PagedResponse;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only user management endpoints. Mounted under {@code /v1/admin/users}
 * (effective gateway path {@code /api/users/v1/admin/users/**}); all routes require ADMIN role.
 */
@Tag(name = "Admin — Users", description = "Admin-only user management operations")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/v1/admin/users")
public interface AdminUserApi {

    @Operation(summary = "List all users", description = "Paginated list of all registered users. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role")
    })
    @GetMapping
    ResponseEntity<PagedResponse<UserDTO>> getAllUsers(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole,
            @PageableDefault(size = 20) Pageable pageable);

    @Operation(summary = "Get user by ID", description = "Returns the full profile for the specified user. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUserById(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole,
            @Parameter(description = "ID of the user to retrieve", required = true) @PathVariable("userId") Long userId) throws Exception;

    @Operation(summary = "Update user by ID", description = "Replaces profile fields for the specified user. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID of the user to update", required = true) @PathVariable("userId") Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole) throws Exception;

    @Operation(summary = "Unlock user account", description = "Clears any active lock and resets the failed-login counter. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account unlocked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/unlock")
    ResponseEntity<UserResponse> unlockAccount(
            @Parameter(description = "ID of the user to unlock", required = true) @PathVariable("userId") Long userId,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole) throws Exception;

    @Operation(summary = "Enable or disable user account", description = "Toggles whether the account can authenticate. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account status updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{userId}/enabled")
    ResponseEntity<UserResponse> setEnabled(
            @Parameter(description = "ID of the user to update", required = true) @PathVariable("userId") Long userId,
            @Parameter(description = "Whether the account should be enabled", required = true) @RequestParam("enabled") boolean enabled,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole) throws Exception;

    @Operation(summary = "Delete user", description = "Permanently removes or deactivates the specified user account. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Caller does not have the ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{username}")
    ResponseEntity<BaseResponse> deleteUser(
            @Parameter(description = "Username of the user to delete", required = true) @PathVariable("username") String username,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String adminRole) throws Exception;
}
