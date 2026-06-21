package com.tu2l.user.controller;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.audit.AuditEventType;
import com.tu2l.user.audit.AuditService;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.ChangePasswordRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.model.response.UserResponse;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1")
@Tag(name = "Users", description = "User profile management")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public UserController(UserService userService, UserMapper userMapper, AuditService auditService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_EMAIL) String userEmail)
            throws Exception {
        log.info("Fetching current user profile");
        UserEntity user = userService.getUserByEmail(userEmail);
        UserDTO userDTO = userMapper.toUserDTO(user);
        UserResponse response = new UserResponse();
        response.setUser(userDTO);
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        log.info("User profile retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching users page: {}", pageable);
        if (!UserRole.ADMIN.name().equals(userRole)) {
            log.warn("Unauthorized access attempt to list all users");
            return ResponseEntity.status(403).build();
        }
        Page<UserDTO> users = userService.getAllUsers(pageable).map(userMapper::toUserDTO);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole,
            @PathVariable Long id) throws Exception {
        log.info("Fetching user profile for ID: {}", id);
        if (!UserRole.ADMIN.name().equals(userRole)) {
            log.warn("Unauthorized access attempt to fetch user ID: {}", id);
            return ResponseEntity.status(403).build();
        }
        UserEntity user = userService.getUserById(id);
        UserDTO userDTO = userMapper.toUserDTO(user);
        UserResponse response = new UserResponse();
        response.setUser(userDTO);
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        log.info("User retrieved successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_EMAIL) String userEmail)
            throws Exception {
        log.info("Updating current user profile");
        UserDTO userDTO = userMapper.toUserDTO(request);
        UserEntity user = userService.updateUser(userDTO);
        auditService.log(AuditEventType.PROFILE_UPDATED, user.getId(), null);
        UserResponse response = new UserResponse();
        response.setUser(userMapper.toUserDTO(user));
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("User profile updated successfully");
        log.info("User profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole)
            throws Exception {
        log.info("Updating user profile for ID: {}", id);
        if (!UserRole.ADMIN.name().equals(userRole)) {
            log.warn("Unauthorized access attempt to update user ID: {}", id);
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

    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_USERNAME) String username)
            throws Exception {
        log.info("Change password for current user request received");
        UserEntity user = userService.updatePassword(username, request.getCurrentPassword(), request.getNewPassword());
        UserResponse response = new UserResponse();
        response.setUser(userMapper.toUserDTO(user));
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("Password changed successfully");
        log.info("Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse> deleteCurrentUser(
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_USERNAME) String username)
            throws Exception {
        log.info("Deleting/deactivating current user account");
        boolean deleted = userService.deleteUser(username);
        if (!deleted) {
            throw new UserException("Failed to delete user account: " + username);
        }
        BaseResponse response = new BaseResponse() {};
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("Account deleted successfully");
        log.info("Account deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<BaseResponse> deleteUser(
            @PathVariable String username,
            @Parameter(hidden = true) @RequestHeader(CommonConstants.Headers.X_USER_ROLE) String userRole)
            throws Exception {
        log.info("Deleting user with username: {}", username);
        if (!UserRole.ADMIN.name().equals(userRole)) {
            log.warn("Unauthorized access attempt to delete user: {}", username);
            return ResponseEntity.status(403).build();
        }
        boolean deleted = userService.deleteUser(username);
        if (!deleted) {
            throw new UserException("Failed to delete user: " + username);
        }
        BaseResponse response = new BaseResponse() {};
        response.setStatus(ResponseProcessingStatus.SUCCESS);
        response.setMessage("User deleted successfully");
        log.info("User deleted successfully: {}", username);
        return ResponseEntity.ok(response);
    }
}
