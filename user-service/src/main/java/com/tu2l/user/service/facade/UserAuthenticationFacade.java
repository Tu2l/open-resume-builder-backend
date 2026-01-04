package com.tu2l.user.service.facade;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.user.entity.UserCredential;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.NewUserRegisterRequest;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.EmailService;
import com.tu2l.user.service.PasswordService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UserAuthenticationFacade {
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final UserMapper userMapper;

    public UserEntity register(NewUserRegisterRequest request) throws UserException {
        if (userService.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new UserException("User already exists with username or email");
        }

        UserEntity user = userMapper.toUserEntity(request);
        user.setPassword(passwordService.hashPassword(request.getPassword()));

        try {
            attachLoginTokens(user);
            log.info("User registered successfully: {}", request.getUsername());
            return userService.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Registration failed due to concurrent requests. Please try again.");
        }
    }

    public UserEntity authenticate(String email, String password, boolean rememberMe)
            throws UserException, AuthenticationException {
        var user = userService.getUserByEmail(email);
        var accountStatus = user.getAccountStatus();

        if (accountStatus.isAccountLocked() || !accountStatus.isEnabled()) {
            throw new AuthenticationException("Account is locked or disabled");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            if (accountStatus.incrementFailedLoginAttempts() >= 5) { // TODO move max failed attempts and lock duration to application.yaml
                accountStatus.lockAccount(15);
                userService.saveUser(user);
                log.warn("User account locked due to multiple failed login attempts: {}", email);
                throw new AuthenticationException("Account locked due to multiple failed login attempts");
            }
            userService.saveUser(user);
            throw new AuthenticationException("Invalid username or password");
        }

        try {
            attachLoginTokens(user);
            log.info("User authenticated successfully: {}", email);
            return userService.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Authentication failed due to concurrent requests. Please try again.");
        }
    }

    public UserEntity refreshToken(String refreshToken, String username)
            throws JwtException, AuthenticationException, UserException {
        var user = userService.getUserByUsername(username);

        var refreshTokenCredential = user.getCredentialByTokenTypeAndToken(JwtTokenType.REFRESH, refreshToken);

        if (refreshTokenCredential == null || refreshTokenCredential.isTokenExpired()) {
            throw new AuthenticationException("Invalid refresh token");
        }

        try {
            var newAccessToken = authTokenService.refreshAccessToken(refreshToken, user);
            user.addUserCredential(buildUserCredential(newAccessToken, JwtTokenType.ACCESS));
            log.info("Token refreshed successfully for user: {}", username);
            return userService.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Token refresh failed due to concurrent requests. Please try again.");
        }
    }

    private void attachLoginTokens(UserEntity user) {
        var refreshToken = authTokenService.generateToken(user, JwtTokenType.REFRESH);
        user.addUserCredential(buildUserCredential(refreshToken, JwtTokenType.REFRESH));

        var accessToken = authTokenService.generateToken(user, JwtTokenType.ACCESS);
        user.addUserCredential(buildUserCredential(accessToken, JwtTokenType.ACCESS));
    }

    private UserCredential buildUserCredential(String accessToken, JwtTokenType tokenType) {
        return UserCredential.builder()
                .token(accessToken)
                .active(true)
                .issuedAt(authTokenService.issuedAt(accessToken))
                .expiresAt(authTokenService.expiresAt(accessToken))
                .issuer("internal-auth-service")
                .tokenType(tokenType)
                .build();
    }

    public boolean invalidateToken(String token) {
        var username = authTokenService.getUsername(token);
        var user = userService.getUserByUsername(username);
        var removed = user.removeCredentialByToken(token);
        userService.saveUser(user);
        return removed;
    }

    public boolean forgotPassword(String email) {
        var userName = userService.getUserByEmail(email);
        var passwordResetToken = authTokenService.generateToken(userName, JwtTokenType.PASSWORD_RESET);
        return emailService.sendPasswordResetEmail(email, passwordResetToken);
    }

    public boolean resetPassword(String passwordResetToken, String newPassword) {
        if (!authTokenService.validateToken(passwordResetToken, JwtTokenType.PASSWORD_RESET)) return false;

        var username = authTokenService.getUsername(passwordResetToken);
        var user = userService.getUserByUsername(username);

        var userCredential = user.getCredentialByTokenTypeAndToken(JwtTokenType.PASSWORD_RESET, passwordResetToken);

        if (userCredential == null) {
            log.warn("Invalid password reset token of user: {}", user.getUsername());
            return false;
        }

        user.setPassword(passwordService.hashPassword(newPassword));
        user.clearSensitiveTokens();
        userService.saveUser(user);
        return true;
    }

    public boolean validateToken(String verificationToken, JwtTokenType jwtTokenType) {
        return authTokenService.validateToken(verificationToken, jwtTokenType);
    }
}
