package com.tu2l.user.service;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.config.BootstrapAdminProperties;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapperTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private AdminBootstrapper bootstrapper(BootstrapAdminProperties props) {
        return new AdminBootstrapper(props, userRepository, passwordEncoder);
    }

    private static BootstrapAdminProperties props(boolean enabled) {
        return new BootstrapAdminProperties(enabled, "admin", "admin@x.com", "Admin@12345");
    }

    @Test
    void seedsAdminWhenEnabledAndNoneExists() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsernameOrEmail("admin", "admin@x.com")).thenReturn(false);
        when(passwordEncoder.encode("Admin@12345")).thenReturn("hashed");

        bootstrapper(props(true)).run(null);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getAccountStatus().isEnabled()).isTrue();
        assertThat(saved.getAccountStatus().isEmailVerified()).isTrue();
    }

    @Test
    void doesNothingWhenDisabled() {
        bootstrapper(props(false)).run(null);
        verify(userRepository, never()).save(any());
    }

    @Test
    void skipsWhenAdminAlreadyExists() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        bootstrapper(props(true)).run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void skipsWhenConfigIncomplete() {
        var incomplete = new BootstrapAdminProperties(true, "admin", "", "pw");
        bootstrapper(incomplete).run(null);
        verify(userRepository, never()).save(any());
    }

    @Test
    void skipsWhenUsernameOrEmailTaken() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsernameOrEmail("admin", "admin@x.com")).thenReturn(true);

        bootstrapper(props(true)).run(null);

        verify(userRepository, never()).save(any());
    }
}
