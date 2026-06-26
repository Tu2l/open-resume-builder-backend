package com.tu2l.user.controller;

import com.tu2l.user.audit.AuditService;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.service.AdminUserService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock AdminUserService adminUserService;
    @Mock UserService userService;
    @Mock UserMapper userMapper;
    @Mock AuditService auditService;

    @InjectMocks AdminUserController controller;

    @Test
    void unlockAccount_nonAdmin_returnsForbidden() {
        var response = controller.unlockAccount(1L, "USER");
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void unlockAccount_admin_returnsOk() {
        var user = UserEntity.builder().id(1L).build();
        when(adminUserService.unlockAccount(1L)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(new UserDTO());

        var response = controller.unlockAccount(1L, "ADMIN");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void setEnabled_nonAdmin_returnsForbidden() {
        var response = controller.setEnabled(1L, false, "USER");
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void setEnabled_admin_returnsOk() {
        var user = UserEntity.builder().id(1L).build();
        when(adminUserService.setEnabled(1L, false)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(new UserDTO());

        var response = controller.setEnabled(1L, false, "ADMIN");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
