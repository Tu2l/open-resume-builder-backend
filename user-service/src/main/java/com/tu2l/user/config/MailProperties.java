package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-level email settings (sender identity + the links embedded in
 * verification / password-reset emails). SMTP transport itself is configured
 * separately under {@code spring.mail.*}.
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        String fromAddress,
        String frontendBaseUrl,
        String verificationPath,
        String passwordResetPath,
        String verificationSubject,
        String passwordResetSubject
) {
    /** Builds the full email-verification link for the given token. */
    public String verificationLink(String token) {
        return frontendBaseUrl + verificationPath + token;
    }

    /** Builds the full password-reset link for the given token. */
    public String passwordResetLink(String token) {
        return frontendBaseUrl + passwordResetPath + token;
    }
}
