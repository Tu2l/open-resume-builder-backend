package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional bootstrap admin, used to solve the first-admin chicken-and-egg problem.
 * When {@code enabled} and no ADMIN account exists yet, an admin is seeded from these
 * values on startup. Disable (or leave unset) once a real admin exists.
 *
 * @param enabled  whether to attempt seeding on startup
 * @param username seed admin username
 * @param email    seed admin email
 * @param password seed admin raw password (will be BCrypt-hashed)
 */
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record BootstrapAdminProperties(
        boolean enabled,
        String username,
        String email,
        String password
) {
}
