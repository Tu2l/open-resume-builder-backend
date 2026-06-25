package com.tu2l.user.controller;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.base.PagedResponse;
import com.tu2l.user.controller.api.AdminUserApi;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.model.response.UserResponse;
import com.tu2l.user.service.AdminUserService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminUserController extends BaseController implements AdminUserApi {

    private final AdminUserService adminUserService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<PagedResponse<UserDTO>> getAllUsers(String adminRole, @PageableDefault(size = 20) Pageable pageable) {
        if (!isAdmin(adminRole)) return forbidden();
        log.info("Admin: listing users, page={}", pageable);
        return ResponseEntity.ok(paged(adminUserService.getAllUsers(pageable), userMapper::toUserDTO));
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(String adminRole, Long userId) {
        if (!isAdmin(adminRole)) return forbidden();
        log.info("Admin: fetching user id={}", userId);
        UserEntity user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.of(userMapper.toUserDTO(user)));
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(Long userId, UpdateUserRequest request, String adminRole) throws Exception {
        if (!isAdmin(adminRole)) return forbidden();
        log.info("Admin: updating user id={}", userId);
        UserDTO userDTO = userMapper.toUserDTO(request);
        userDTO.setId(userId);
        UserEntity user = userService.updateUser(userDTO);
        return ResponseEntity.ok(UserResponse.of(userMapper.toUserDTO(user), "User profile updated successfully"));
    }

    @Override
    public ResponseEntity<BaseResponse> deleteUser(String username, String adminRole) throws Exception {
        if (!isAdmin(adminRole)) return forbidden();
        log.info("Admin: deleting user username={}", username);
        if (!userService.deleteUser(username)) throw new UserException("Failed to delete user: " + username);
        return success("User deleted successfully");
    }
}
