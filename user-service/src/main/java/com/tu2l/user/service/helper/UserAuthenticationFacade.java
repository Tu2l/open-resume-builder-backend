package com.tu2l.user.service.helper;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UserAuthenticationFacade {
    private final CommonUtil commonUtil;
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserEntity register(RegisterRequest request) throws UserException {
        if (userService.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new UserException("User already exists with username or email");
        }

        UserEntity user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(commonUtil.decodeBase64StringToString(request.getPassword())));

        try {
            attachLoginTokens(user);
            log.info("User registered successfully: {}", request.getUsername());
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserException("Registration failed due to concurrent requests. Please try again.");
        }
    }

    public UserEntity authenticate(String usernameOrEmail, String password, boolean rememberMe)
            throws UserException, AuthenticationException {
        var user = commonUtil.isValidEmail(usernameOrEmail)
                ? userService.getUserByEmail(usernameOrEmail)
                : userService.getUserByUsername(usernameOrEmail);

        if (user.isAccountLocked() || !user.getEnabled()) {
            throw new AuthenticationException("Account is locked or disabled");
        }

        if (!passwordEncoder.matches(commonUtil.decodeBase64StringToString(password), user.getPassword())) {
            if (user.incrementFailedLoginAttempts() >= 5) {
                user.lockAccount(15);
                userRepository.save(user);
                log.warn("User account locked due to multiple failed login attempts: {}", usernameOrEmail);
                throw new AuthenticationException("Account locked due to multiple failed login attempts");
            }
            userRepository.save(user);
            throw new AuthenticationException("Invalid username or password");
        }

        try {
            attachLoginTokens(user);
            log.info("User authenticated successfully: {}", usernameOrEmail);
            return userRepository.save(user);
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
            return userRepository.save(user);
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
}
