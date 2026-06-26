package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthConfigValues(
        int maxFailedLoginAttempts,
        int accountLockDurationMinutes,
        int rememberMeTokenValidityMinutes,
        boolean requireVerifiedEmail
) {
}
