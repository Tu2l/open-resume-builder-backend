package com.tu2l.user.service.facade;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.EmailService;
import com.tu2l.user.service.PasswordService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
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

    public UserEntity register(RegisterRequest request) throws UserException {
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


        if (user.isAccountLocked() || !user.getEnabled()) {
            throw new AuthenticationException("Account is locked or disabled");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            if (user.incrementFailedLoginAttempts() >= 5) { // TODO move max failed attempts and lock duration to application.yaml
                user.lockAccount(15);
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

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new AuthenticationException("Invalid refresh token");
        }

        try {
            var newAccessToken = authTokenService.refreshAccessToken(refreshToken, user);
            user.addUserLogin(buildUserLogin(newAccessToken));
            log.info("Token refreshed successfully for user: {}", username);
            return userService.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Token refresh failed due to concurrent requests. Please try again.");
        }
    }

    private void attachLoginTokens(UserEntity user) {
        var refreshToken = authTokenService.generateToken(user, JwtTokenType.REFRESH);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(authTokenService.issuedAt(refreshToken)
                .plusSeconds(authTokenService.getTokenRemainingTime(refreshToken)));

        var accessToken = authTokenService.generateToken(user, JwtTokenType.ACCESS);
        user.addUserLogin(buildUserLogin(accessToken));
    }

    private UserLogin buildUserLogin(String accessToken) {
        return UserLogin.builder()
                .token(accessToken)
                .expiresIn(authTokenService.getTokenRemainingTime(accessToken))
                .loggedInAt(authTokenService.issuedAt(accessToken))
                .build();
    }

    public boolean invalidateToken(String token) {
        var username = authTokenService.getUsername(token);
        var user = userService.getUserByUsername(username);
        var removed = user.getUserLogins().removeIf(login -> login.getToken().equals(token));
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

        if (StringUtils.isBlank(user.getPasswordResetToken()) || !user.getPasswordResetToken().equals(passwordResetToken)) {
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
