package com.tu2l.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.model.request.ChangePasswordRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserResponse;
import com.tu2l.user.service.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * User Controller - Handles user profile management operations
 * Base path: /user/users
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users/me - Get current authenticated user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Fetching current user profile");
        // TODO: Extract user from JWT token in Authorization header
        UserResponse response = new UserResponse();
        response.setMessage("User profile retrieved successfully");
        log.info("User profile retrieved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /users/{id} - Get user by ID (Admin only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Fetching user profile for ID: {}", id);
        // TODO: Authorization check - admin only
        UserResponse response = new UserResponse();
        response.setMessage("User retrieved successfully");
        log.info("User retrieved successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/me - Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Updating current user profile");
        // TODO: Extract user from JWT and update profile
        UserResponse response = new UserResponse();
        response.setMessage("User profile updated successfully");
        log.info("User profile updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/{id} - Update user by ID (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Updating user profile for ID: {}", id);
        // TODO: Authorization check - admin only
        UserResponse response = new UserResponse();
        response.setMessage("User updated successfully");
        log.info("User updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/me/password - Change current user password
     */
    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Changing password for current user");
        // TODO: Extract user from JWT, verify current password and update
        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Password changed successfully");
        log.info("Password changed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /users/me - Delete/deactivate current user account
     */
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse> deleteCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Deleting/deactivating current user account");
        // TODO: Extract user from JWT and soft delete/deactivate
        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Account deleted successfully");
        log.info("Account deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /users/{id} - Delete user by ID (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        // TODO: Authorization check - admin only
        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("User deleted successfully");
        log.info("User deleted successfully: {}", id);
        return ResponseEntity.ok(response);
    }
}
