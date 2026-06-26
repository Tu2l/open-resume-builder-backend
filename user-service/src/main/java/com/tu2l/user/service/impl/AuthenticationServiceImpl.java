package com.tu2l.user.service.impl;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.audit.AuditEventType;
import com.tu2l.user.audit.AuditService;
import com.tu2l.user.config.AuthConfigValues;
import com.tu2l.user.entity.UserCredential;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.DuplicateUserException;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.NewUserRegisterRequest;
import com.tu2l.user.service.*;
import com.tu2l.user.utils.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final UserMapper userMapper;
    private final AuthConfigValues authConfigValues;
    private final CommonUtil commonUtil;
    private final AuditService auditService;

    @Override
    public UserEntity register(NewUserRegisterRequest request) throws UserException {
        if (userService.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new DuplicateUserException("User already exists with username or email");
        }

        UserEntity user = userMapper.toUserEntity(request);
        user.setPassword(passwordService.hashPassword(request.getPassword()));

        try {
            attachLoginTokens(user);
            var verificationToken = authTokenService.generateToken(user, JwtTokenType.EMAIL_VERIFICATION);
            user.addUserCredential(buildUserCredential(verificationToken, JwtTokenType.EMAIL_VERIFICATION));
            var saved = userService.saveUser(user);
            emailService.sendVerificationEmail(saved.getEmail(), verificationToken);
            auditService.log(AuditEventType.USER_REGISTERED, saved.getId(), saved.getUsername());
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Registration failed due to concurrent requests. Please try again.");
        }
    }

    @Override
    // Do not roll back on AuthenticationException, otherwise the failed-attempt
    // increment / account lock below would be undone and accounts would never lock.
    @Transactional(noRollbackFor = AuthenticationException.class)
    public UserEntity authenticate(String email, String password, boolean rememberMe) throws UserException, AuthenticationException {
        var user = userService.getUserByEmailWithDetails(email);
        var accountStatus = user.getAccountStatus();

        // A previously-set lock that has elapsed must reset the failed-attempt counter,
        // otherwise the next single wrong password would re-lock immediately.
        if (accountStatus.isLockExpired()) {
            accountStatus.clearExpiredLock();
        }

        if (accountStatus.isAccountLocked() || !accountStatus.isEnabled()) {
            auditService.log(AuditEventType.LOGIN_FAILED, user.getId(), "account locked or disabled");
            throw new AuthenticationException("Account is locked or disabled");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            if (accountStatus.incrementFailedLoginAttempts() >= authConfigValues.maxFailedLoginAttempts()) {
                accountStatus.lockAccount(authConfigValues.accountLockDurationMinutes());
                userService.saveUser(user);
                log.warn("User account locked due to multiple failed login attempts: {}", email);
                auditService.log(AuditEventType.ACCOUNT_LOCKED, user.getId(), "too many failed login attempts");
                throw new AuthenticationException("Account locked due to multiple failed login attempts");
            }
            userService.saveUser(user);
            auditService.log(AuditEventType.LOGIN_FAILED, user.getId(), "invalid password");
            throw new AuthenticationException("Invalid username or password");
        }

        // Password is correct; optionally require a verified email before issuing tokens.
        if (authConfigValues.requireVerifiedEmail() && !accountStatus.isEmailVerified()) {
            auditService.log(AuditEventType.LOGIN_FAILED, user.getId(), "email not verified");
            throw new AuthenticationException("Email is not verified");
        }

        try {
            // Successful login: clear any prior failed attempts and record the time.
            accountStatus.unlockAccount();
            accountStatus.setLastLoginAt(LocalDateTime.now());
            attachLoginTokens(user);
            log.info("User authenticated successfully: {}", email);
            var saved = userService.saveUser(user);
            auditService.log(AuditEventType.LOGIN_SUCCESS, saved.getId(), null);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Authentication failed due to concurrent requests. Please try again.");
        }
    }

    @Override
    public UserEntity refreshToken(String refreshToken, String username) throws JwtException, AuthenticationException, UserException {
        var user = userService.getUserWithCredentials(username);

        var refreshTokenCredential = user.getCredentialByTokenTypeAndToken(JwtTokenType.REFRESH, commonUtil.sha256Hex(refreshToken));

        if (refreshTokenCredential == null || refreshTokenCredential.isTokenExpired()) {
            throw new AuthenticationException("Invalid refresh token");
        }

        // Reuse detection: presenting a refresh token that was already rotated (and thus
        // deactivated) indicates the token leaked. Revoke every session for safety.
        if (Boolean.FALSE.equals(refreshTokenCredential.getActive())) {
            log.warn("Refresh-token reuse detected for user: {}; revoking all sessions", username);
            user.clearSensitiveTokens();
            userService.saveUser(user);
            auditService.log(AuditEventType.TOKEN_REUSE_DETECTED, user.getId(), "refresh token reuse");
            throw new AuthenticationException("Refresh token reuse detected");
        }

        try {
            // Rotate: deactivate the presented refresh token and mint a brand-new one
            // alongside the new access token (refresh-token rotation).
            refreshTokenCredential.setActive(false);

            var newRefreshToken = authTokenService.generateToken(user, JwtTokenType.REFRESH);
            user.addUserCredential(buildUserCredential(newRefreshToken, JwtTokenType.REFRESH));

            var newAccessToken = authTokenService.refreshAccessToken(refreshToken, user);
            user.addUserCredential(buildUserCredential(newAccessToken, JwtTokenType.ACCESS));

            // Carry the freshly-minted raw tokens back for the response.
            user.setPlainAccessToken(newAccessToken);
            user.setPlainRefreshToken(newRefreshToken);
            log.info("Token refreshed (rotated) successfully for user: {}", username);
            var saved = userService.saveUser(user);
            auditService.log(AuditEventType.TOKEN_REFRESHED, saved.getId(), null);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Token refresh failed due to concurrent requests. Please try again.");
        }
    }

    @Override
    public boolean logout(String token) throws JwtException, AuthenticationException {
        var username = authTokenService.getUsername(token);
        var user = userService.getUserWithCredentials(username);
        var removed = user.removeCredentialByToken(commonUtil.sha256Hex(token));
        userService.saveUser(user);
        auditService.log(AuditEventType.LOGOUT, user.getId(), null);
        return removed;
    }

    @Override
    public boolean forgotPassword(String email) throws JwtException, AuthenticationException {
        // Uniform response whether or not the email exists, to avoid account enumeration.
        var userOpt = userService.findByEmailWithCredentials(email);
        if (userOpt.isEmpty()) {
            log.info("Forgot-password requested for an unknown email; returning success without sending mail");
            return true;
        }
        var user = userOpt.get();
        var passwordResetToken = authTokenService.generateToken(user, JwtTokenType.PASSWORD_RESET);
        // Persist the reset-token credential (hashed) so resetPassword can validate it.
        user.addUserCredential(buildUserCredential(passwordResetToken, JwtTokenType.PASSWORD_RESET));
        userService.saveUser(user);
        auditService.log(AuditEventType.PASSWORD_RESET_REQUESTED, user.getId(), null);
        emailService.sendPasswordResetEmail(email, passwordResetToken);
        return true;
    }

    @Override
    public boolean resetPassword(String passwordResetToken, String newPassword) throws JwtException, UserException, AuthenticationException {
        if (!authTokenService.validateToken(passwordResetToken, JwtTokenType.PASSWORD_RESET)) return false;

        var username = authTokenService.getUsername(passwordResetToken);
        var user = userService.getUserWithCredentials(username);

        var userCredential = user.getCredentialByTokenTypeAndToken(JwtTokenType.PASSWORD_RESET, commonUtil.sha256Hex(passwordResetToken));

        if (userCredential == null) {
            log.warn("Invalid password reset token of user: {}", user.getUsername());
            return false;
        }

        user.setPassword(passwordService.hashPassword(newPassword));
        user.clearSensitiveTokens();
        userService.saveUser(user);
        auditService.log(AuditEventType.PASSWORD_RESET_COMPLETED, user.getId(), null);
        return true;
    }

    @Override
    public boolean verifyEmail(String verificationToken) throws JwtException, AuthenticationException {
        if (!authTokenService.validateToken(verificationToken, JwtTokenType.EMAIL_VERIFICATION)) {
            return false;
        }
        var username = authTokenService.getUsername(verificationToken);
        var user = userService.getUserWithCredentials(username);

        var hashedToken = commonUtil.sha256Hex(verificationToken);
        var credential = user.getCredentialByTokenTypeAndToken(JwtTokenType.EMAIL_VERIFICATION, hashedToken);
        if (credential == null) {
            // Token already consumed (or never issued) — reject the replay.
            log.warn("Invalid or already-used email verification token for user: {}", username);
            return false;
        }

        user.getAccountStatus().setEmailVerified(true);
        user.removeCredentialByToken(hashedToken); // one-time use
        userService.saveUser(user);
        auditService.log(AuditEventType.EMAIL_VERIFIED, user.getId(), null);
        log.info("Email verified for user: {}", username);
        return true;
    }

    @Override
    public boolean resendVerification(String email) throws JwtException, AuthenticationException {
        // Uniform response whether or not the email exists / is already verified.
        var userOpt = userService.findByEmailWithCredentials(email);
        if (userOpt.isEmpty()) {
            log.info("Resend-verification requested for an unknown email; returning success without sending mail");
            return true;
        }
        var user = userOpt.get();
        if (user.getAccountStatus().isEmailVerified()) {
            log.info("Resend-verification requested for an already-verified account; no mail sent");
            return true;
        }
        var verificationToken = authTokenService.generateToken(user, JwtTokenType.EMAIL_VERIFICATION);
        user.addUserCredential(buildUserCredential(verificationToken, JwtTokenType.EMAIL_VERIFICATION));
        userService.saveUser(user);
        auditService.log(AuditEventType.VERIFICATION_RESENT, user.getId(), null);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        return true;
    }

    private void attachLoginTokens(UserEntity user) {
        var refreshToken = authTokenService.generateToken(user, JwtTokenType.REFRESH);
        user.addUserCredential(buildUserCredential(refreshToken, JwtTokenType.REFRESH));

        var accessToken = authTokenService.generateToken(user, JwtTokenType.ACCESS);
        user.addUserCredential(buildUserCredential(accessToken, JwtTokenType.ACCESS));

        // Carry the raw tokens back for the auth response (only hashes are persisted).
        user.setPlainRefreshToken(refreshToken);
        user.setPlainAccessToken(accessToken);
    }

    private UserCredential buildUserCredential(String rawToken, JwtTokenType tokenType) {
        return UserCredential.builder()
                .token(commonUtil.sha256Hex(rawToken))
                .active(true)
                .issuedAt(authTokenService.issuedAt(rawToken))
                .expiresAt(authTokenService.expiresAt(rawToken))
                .issuer("internal-auth-service")
                .tokenType(tokenType)
                .build();
    }
}
