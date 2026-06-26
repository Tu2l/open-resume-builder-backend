package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.entity.UserAccountStatus;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.exception.UserNotFoundException;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.utils.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CommonUtil commonUtil;
    @Mock UserMapper userMapper;

    @InjectMocks UserServiceImpl userService;

    @Test
    void updateUser_nullDto_throwsBadRequest() {
        assertThatThrownBy(() -> userService.updateUser(null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void updateUser_nullId_throwsBadRequest() {
        assertThatThrownBy(() -> userService.updateUser(new UserDTO()))
                .isInstanceOf(UserException.class);
    }

    @Test
    void updateUser_missingUser_throwsNotFound() {
        UserDTO dto = new UserDTO();
        dto.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_persistsMappedEntity() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        UserEntity existing = UserEntity.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userMapper.updateUserFromDTO(dto, existing)).thenReturn(existing);
        when(userRepository.save(existing)).thenReturn(existing);

        assertThat(userService.updateUser(dto)).isSameAs(existing);
    }

    @Test
    void getUserById_missing_throwsNotFound() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(7L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void unlockAccount_clearsLockAndResetsCounter() {
        UserAccountStatus status = new UserAccountStatus();
        status.setAccountLockedUntil(LocalDateTime.now().plusMinutes(10));
        status.setFailedLoginAttempts(5);
        UserEntity user = UserEntity.builder().id(1L).accountStatus(status).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        userService.unlockAccount(1L);

        assertThat(status.getAccountLockedUntil()).isNull();
        assertThat(status.getFailedLoginAttempts()).isZero();
    }

    @Test
    void setEnabled_togglesFlag() {
        UserAccountStatus status = new UserAccountStatus();
        status.setEnabled(true);
        UserEntity user = UserEntity.builder().id(1L).accountStatus(status).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        userService.setEnabled(1L, false);

        assertThat(status.isEnabled()).isFalse();
    }
}
