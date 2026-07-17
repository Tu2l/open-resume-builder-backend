package com.tu2l.user.controller;

import com.tu2l.user.audit.AuditService;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
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
class UserControllerTest {

    @Mock UserService userService;
    @Mock UserMapper userMapper;
    @Mock AuditService auditService;

    @InjectMocks UserController controller;

    @Test
    void updateCurrentUser_resolvesAuthenticatedUserIdBeforeUpdating() throws Exception {
        var request = new UpdateUserRequest();
        var current = UserEntity.builder().id(42L).build();
        var mapped = new UserDTO();
        var updated = UserEntity.builder().id(42L).build();

        when(userService.getUserByEmail("john@example.com")).thenReturn(current);
        when(userMapper.toUserDTO(request)).thenReturn(mapped);
        when(userService.updateUser(mapped)).thenReturn(updated);
        when(userMapper.toUserDTO(updated)).thenReturn(new UserDTO());

        var response = controller.updateCurrentUser(request, "john@example.com");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        // The request carries no id; the controller must inject the authenticated user's id.
        assertThat(mapped.getId()).isEqualTo(42L);
    }
}
