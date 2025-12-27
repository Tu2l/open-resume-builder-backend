package com.tu2l.user.service.impl;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final CommonUtil commonUtil;
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthenticationServiceImpl(CommonUtil commonUtil, UserService userService, AuthTokenService authTokenService,
                                     UserMapper userMapper, PasswordEncoder passwordEncoder,
                                     UserRepository userRepository) {
        this.commonUtil = commonUtil;
        this.userService = userService;
        this.authTokenService = authTokenService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity register(RegisterRequest request) throws UserException {
        // Register the user if not exists
        if (userService.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new UserException("User already exists with username or email");
        }

        UserEntity user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Long userId = Optional.of(userRepository.save(user).getId())
                .orElseThrow(() -> new UserException("Failed to register user"));

        // generate access-token and refresh-token
        user.setId(userId);
        generateTokens(user);

        log.info("User registered successfully: {}", request.getUsername());

        return userRepository.save(user);
    }


    @Override
    public UserEntity authenticate(String usernameOrEmail, String password, boolean rememberMe) throws UserException, AuthenticationException {
        // TODO - handle rememberMe functionality
        var userEntity = commonUtil.isValidEmail(usernameOrEmail) ?
                userService.getUserByEmail(usernameOrEmail) : userService.getUserByUsername(usernameOrEmail);

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new UserException("Invalid username or password");
        }
        generateTokens(userEntity);
        log.info("User authenticated successfully: {}", usernameOrEmail);
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity refreshToken(String refreshToken, String username) throws JwtException, AuthenticationException, UserException {
        var user = userService.getUserByUsername(username);

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new AuthenticationException("Invalid refresh token");
        }

        var accessToken = authTokenService.refreshAccessToken(refreshToken, username);
        UserLogin userLogin = generateUserLogin(accessToken);

        user.addUserLogin(userLogin);
        log.info("Token refreshed successfully for user: {}", username);
        return userRepository.save(user);
    }

    @Override
    public void logout(String token) throws JwtException, AuthenticationException {

    }

    @Override
    public UserEntity forgotPassword(String email) throws JwtException, AuthenticationException {
        return null;
    }

    @Override
    public UserEntity resetPassword(String passwordResetToken, String newPassword) throws JwtException, UserException, AuthenticationException {
        return null;
    }

    @Override
    public UserEntity verifyEmail(String verificationToken) throws JwtException, AuthenticationException {
        return null;
    }

    private void generateTokens(UserEntity user) {
        user.setRefreshToken(authTokenService.generateToken(user, JwtTokenType.REFRESH));
        UserLogin userLogin = generateUserLogin(authTokenService.generateToken(user, JwtTokenType.ACCESS));
        user.addUserLogin(userLogin);
    }

    private UserLogin generateUserLogin(String accessToken) {
        return UserLogin.builder()
                .token(accessToken)
//                .expiresIn(accessToken.getExpirationInMillis(JwtTokenType.ACCESS))
                .expiresIn(500l) // TODO update with real expiration
                .loggedInAt(java.time.LocalDateTime.now())
                .build();
    }
}
