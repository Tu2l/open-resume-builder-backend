package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtUtil jwtUtil;
    private final CommonUtil commonUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthenticationServiceImpl(JwtUtil jwtUtil, CommonUtil commonUtil, UserService userService,
                                     UserMapper userMapper, PasswordEncoder passwordEncoder,
                                     UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.commonUtil = commonUtil;
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity register(RegisterRequest request) throws Exception {
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
    public UserEntity authenticate(String usernameOrEmail, String password, boolean rememberMe) throws Exception {
        // TODO - handle rememberMe functionality
        UserEntity userEntity = commonUtil.isValidEmail(usernameOrEmail) ?
                userService.getUserByEmail(usernameOrEmail) : userService.getUserByUsername(usernameOrEmail);

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new UserException("Invalid username or password");
        }
        generateTokens(userEntity);
        log.info("User authenticated successfully: {}", usernameOrEmail);
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity refreshToken(String refreshToken) throws Exception {
        return null;
    }

    @Override
    public void logout(String token) throws Exception {

    }

    @Override
    public UserEntity forgotPassword(String email) throws Exception {
        return null;
    }

    @Override
    public UserEntity resetPassword(String passwordResetToken, String newPassword) throws Exception {
        return null;
    }

    @Override
    public UserEntity verifyEmail(String verificationToken) throws Exception {
        return null;
    }

    private void generateTokens(UserEntity user) {
        user.setRefreshToken(jwtUtil.generateRefreshToken(user.getUsername()));
        UserLogin userLogin = UserLogin.builder()
                .token(jwtUtil.generateAccessToken(user.getUsername(), user.getEmail(), user.getRole().toString()))
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .loggedInAt(java.time.LocalDateTime.now())
                .build();
        user.addUserLogin(userLogin);
    }
}
