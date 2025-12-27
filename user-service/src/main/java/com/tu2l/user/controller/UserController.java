package com.tu2l.user.controller;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.ChangePasswordRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.model.response.UserResponse;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller - Handles user profile management operations
 */
@Slf4j
@RestController
@RequestMapping("/")
public class UserController {
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final UserMapper userMapper;

    public UserController(UserService userService, AuthTokenService authTokenService, UserMapper userMapper) {
        this.userService = userService;
        this.authTokenService = authTokenService;
        this.userMapper = userMapper;
    }

    /**
     * GET /users/me - Get current authenticated user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader)
            throws Exception {
        log.info("Fetching current user profile");

        Long userId = authTokenService.getUserId(authHeader);
        UserEntity user = userService.getUserById(userId);
        UserDTO userDTO = userMapper.toUserDTO(user);
        UserResponse response = new UserResponse();
        response.setUser(userDTO);
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("User profile retrieved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /users/{id} - Get user by ID (Admin only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@RequestHeader("Authorization") String authHeader,
                                                    @PathVariable Long id) throws Exception {
        log.info("Fetching user profile for ID: {}", id);

        if (!authTokenService.verifyRole(authHeader, UserRole.ADMIN)) {
            log.warn("Unauthorized access attempt to fetch user ID: {}", id);
            return ResponseEntity.status(403).build();
        }

        UserResponse response = new UserResponse();
        UserEntity user = userService.getUserById(id);
        UserDTO userDTO = userMapper.toUserDTO(user);
        response.setUser(userDTO);
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("User retrieved successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/me - Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request,
                                                          @RequestHeader("Authorization") String authHeader) throws Exception {
        log.info("Updating current user profile");

        UserDTO userDTO = userMapper.toUserDTO(request);
        userDTO.setId(authTokenService.getUserId(authHeader));

        UserEntity user = userService.updateUser(userDTO);

        UserResponse response = new UserResponse();
        response.setUser(userMapper.toUserDTO(user));
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("User profile updated successfully");

        log.info("User profile updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/{id} - Update user by ID (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest request,
                                                   @RequestHeader("Authorization") String authHeader) throws Exception {

        log.info("Updating user profile for ID: {}", id);
        if (!authTokenService.verifyRole(authHeader, UserRole.ADMIN)) {
            log.warn("Unauthorized access attempt to fetch user ID: {}", id);
            return ResponseEntity.status(403).build();
        }

        UserDTO userDTO = userMapper.toUserDTO(request);
        userDTO.setId(id);

        UserEntity user = userService.updateUser(userDTO);

        UserResponse response = new UserResponse();
        response.setUser(userMapper.toUserDTO(user));
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("User profile updated successfully");

        log.info("User updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /users/me/password - Change current user password
     */
    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                       @RequestHeader("Authorization") String authHeader) throws Exception {
        log.info("Chnage password for current user request received");
        if (!authTokenService.validateToken(authHeader, JwtTokenType.ACCESS)) {
            throw new AuthenticationException("Invalid access token");
        }

        long userId = authTokenService.getUserId(authHeader);
        UserEntity user = userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        UserResponse response = new UserResponse();
        response.setUser(userMapper.toUserDTO(user));
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("Password changed successfully");

        log.info("Password changed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /users/me - Delete/deactivate current user account
     */
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse> deleteCurrentUser(@RequestHeader("Authorization") String authHeader)
            throws Exception {
        log.info("Deleting/deactivating current user account");
        if (!authTokenService.validateToken(authHeader, JwtTokenType.ACCESS)) {
            throw new AuthenticationException("Invalid access token");
        }

        long userId = authTokenService.getUserId(authHeader);
        boolean deleted = userService.deleteUser(userId);

        if (!deleted) {
            throw new UserException("Failed to delete user account with ID: " + userId);
        }

        BaseResponse response = new BaseResponse() {
        };
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("Account deleted successfully");
        log.info("Account deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /users/{id} - Delete user by ID (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteUser(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String authHeader) throws Exception {
        log.info("Deleting user with ID: {}", id);
        if (!authTokenService.verifyRole(authHeader, UserRole.ADMIN)) {
            log.warn("Unauthorized access attempt to delete user ID: {}", id);
            return ResponseEntity.status(403).build();
        }
        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            throw new UserException("Failed to delete user with ID: " + id);
        }

        BaseResponse response = new BaseResponse() {
        };
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("User deleted successfully");

        log.info("User deleted successfully: {}", id);
        return ResponseEntity.ok(response);
    }
}
