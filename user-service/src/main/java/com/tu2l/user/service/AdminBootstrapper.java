package com.tu2l.user.service;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.config.BootstrapAdminProperties;
import com.tu2l.user.entity.UserAccountStatus;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a single ADMIN account on startup when enabled and none exists yet, solving the
 * first-admin chicken-and-egg (registration always creates USER; promotion requires an
 * existing admin). Idempotent: skips if any admin is already present.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapper implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        if (isBlank(properties.username()) || isBlank(properties.email()) || isBlank(properties.password())) {
            log.warn("Bootstrap admin is enabled but username/email/password are not fully configured; skipping");
            return;
        }
        if (userRepository.existsByRoleIncludingDeleted(UserRole.ADMIN.name())) {
            log.info("Bootstrap admin: an ADMIN account already exists (active or soft-deleted); skipping seed");
            return;
        }
        if (userRepository.existsByUsernameOrEmail(properties.username(), properties.email())) {
            log.warn("Bootstrap admin: username/email '{}' is already taken; skipping seed", properties.username());
            return;
        }

        UserEntity admin = UserEntity.builder()
                .username(properties.username())
                .email(properties.email())
                .role(UserRole.ADMIN)
                .password(passwordEncoder.encode(properties.password()))
                .accountStatus(UserAccountStatus.builder()
                        .enabled(true)
                        .emailVerified(true)
                        .build())
                .build();
        userRepository.save(admin);
        log.info("Bootstrap admin seeded: username='{}', email='{}'", properties.username(), properties.email());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
