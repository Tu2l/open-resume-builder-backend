package com.tu2l.user.controller;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.audit.AuditEventType;
import com.tu2l.user.audit.AuditService;
import com.tu2l.user.controller.api.UserApi;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.ChangePasswordRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.model.response.UserResponse;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController extends BaseController implements UserApi {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    public ResponseEntity<UserResponse> getCurrentUser(String userEmail) throws Exception {
        log.info("Fetching current user profile");
        UserEntity user = userService.getUserByEmailWithDetails(userEmail);
        return ResponseEntity.ok(UserResponse.of(userMapper.toUserDTO(user)));
    }

    @Override
    public ResponseEntity<UserResponse> updateCurrentUser(UpdateUserRequest request, String userEmail) throws Exception {
        log.info("Updating current user profile");
        // Resolve the authenticated user so the update targets the right id; the request
        // body carries no id, only the profile fields to change.
        UserEntity current = userService.getUserByEmail(userEmail);
        UserDTO dto = userMapper.toUserDTO(request);
        dto.setId(current.getId());
        UserEntity user = userService.updateUser(dto);
        auditService.log(AuditEventType.PROFILE_UPDATED, user.getId(), null);
        return ResponseEntity.ok(UserResponse.of(userMapper.toUserDTO(user), "User profile updated successfully"));
    }

    @Override
    public ResponseEntity<UserResponse> changePassword(ChangePasswordRequest request, String username) throws Exception {
        log.info("Change password for current user");
        UserEntity user = userService.updatePassword(username, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(UserResponse.of(userMapper.toUserDTO(user), "Password changed successfully"));
    }

    @Override
    public ResponseEntity<BaseResponse> deleteCurrentUser(String username) throws Exception {
        log.info("Deleting/deactivating current user account");
        if (!userService.deleteUser(username)) throw new UserException("Failed to delete account: " + username);
        return success("Account deleted successfully");
    }
}
