package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.audit.AuditService;
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

import com.tu2l.user.audit.AuditEventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CommonUtil commonUtil;
    @Mock UserMapper userMapper;
    @Mock AuditService auditService;

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
    void updatePassword_validOldPassword_encodesNewAndAudits() {
        UserEntity user = UserEntity.builder().id(1L).username("john").password("oldHash").build();
        when(commonUtil.decodeBase64StringToString("bmV3")).thenReturn("newPlain");
        when(userRepository.findUserByUsername("john")).thenReturn(Optional.of(user));
        when(commonUtil.decodeBase64StringToString("b2xk")).thenReturn("oldPlain");
        when(passwordEncoder.matches("oldPlain", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPlain")).thenReturn("newHash");
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.updatePassword("john", "b2xk", "bmV3");

        assertThat(result.getPassword()).isEqualTo("newHash");
        verify(auditService).log(AuditEventType.PASSWORD_CHANGED, 1L, null);
    }

    @Test
    void updatePassword_wrongOldPassword_throwsAndDoesNotAudit() {
        UserEntity user = UserEntity.builder().id(1L).username("john").password("oldHash").build();
        when(commonUtil.decodeBase64StringToString("bmV3")).thenReturn("newPlain");
        when(userRepository.findUserByUsername("john")).thenReturn(Optional.of(user));
        when(commonUtil.decodeBase64StringToString("YmFk")).thenReturn("bad");
        when(passwordEncoder.matches("bad", "oldHash")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword("john", "YmFk", "bmV3"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Old password does not match");
        verify(auditService, never()).log(any(), any(), any());
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
