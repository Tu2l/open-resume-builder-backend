package com.tu2l.user.service.impl;

import com.tu2l.user.config.MailProperties;
import com.tu2l.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Development email implementation: logs the verification / password-reset links
 * instead of sending real email, so the flows work end-to-end without SMTP
 * credentials. Active in every profile except {@code prod}, where
 * {@link SmtpEmailService} takes over (so exactly one EmailService bean exists).
 */
@Slf4j
@Service
@Profile("!prod")
@RequiredArgsConstructor
public class LoggingEmailService implements EmailService {

    private final MailProperties mailProperties;

    @Override
    public boolean sendVerificationEmail(String to, String payload) {
        log.info("[DEV EMAIL] Email verification link for {} -> {}", to, mailProperties.verificationLink(payload));
        return true;
    }

    @Override
    public boolean sendPasswordResetEmail(String to, String payload) {
        log.info("[DEV EMAIL] Password-reset link for {} -> {}", to, mailProperties.passwordResetLink(payload));
        return true;
    }
}
