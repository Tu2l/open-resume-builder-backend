package com.tu2l.user.service.impl;

import com.tu2l.user.config.MailProperties;
import com.tu2l.user.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Production email implementation backed by SMTP, configured via
 * {@code spring.mail.*}. Failures are logged and reported as {@code false}
 * rather than propagated, so a mail outage never breaks the calling flow.
 */
@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public boolean sendVerificationEmail(String to, String payload) {
        return send(to, mailProperties.verificationSubject(),
                "Please verify your email address by visiting: " + mailProperties.verificationLink(payload));
    }

    @Override
    public boolean sendPasswordResetEmail(String to, String payload) {
        return send(to, mailProperties.passwordResetSubject(),
                "Reset your password by visiting: " + mailProperties.passwordResetLink(payload));
    }

    private boolean send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailProperties.fromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to send email to {}", to, e);
            return false;
        }
    }
}
