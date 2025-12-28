package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.user.controller.api.AuthenticationApi;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.model.request.*;
import com.tu2l.user.model.response.AuthResponse;
import com.tu2l.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller - Handles authentication operations
 * Base path: /users/auth
 */
@Slf4j
@RestController
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());

        UserEntity registeredUser = authenticationService.register(request);
        UserLogin userLogin = registeredUser.getMostRecentLogin();

        AuthResponse response = AuthResponse.builder()
                .accessToken(userLogin.getToken())
                .expiresIn(userLogin.getExpiresIn())
                .refreshToken(registeredUser.getRefreshToken())
                .build();

        response.setMessage("User registered successfully");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<@NonNull AuthResponse> authenticate(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        UserEntity loggedInUserEntity = authenticationService.authenticate(request.getUsernameOrEmail(),
                request.getPassword(), request.getRememberMe());
        UserLogin userLogin = loggedInUserEntity.getMostRecentLogin();

        AuthResponse response = AuthResponse.builder()
                .accessToken(userLogin.getToken())
                .expiresIn(userLogin.getExpiresIn())
                .refreshToken(loggedInUserEntity.getRefreshToken())
                .build();

        response.setMessage("Login successful");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Login successful for user: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<@NonNull AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        UserEntity refreshedUserEntity = authenticationService.refreshToken(request.getRefreshToken(), request.getUsername());

        AuthResponse response = AuthResponse.builder()
                .accessToken(refreshedUserEntity.getMostRecentLogin().getToken())
                .expiresIn(refreshedUserEntity.getMostRecentLogin().getExpiresIn())
                .refreshToken(refreshedUserEntity.getRefreshToken())
                .build();

        response.setMessage("Token refreshed successfully");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> logout(String token) {
        log.info("Logout request received");

        authenticationService.logout(token);

        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Logout successful");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Logout successful");
        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<@NonNull BaseResponse> forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());

        authenticationService.forgotPassword(request.getEmail());

        BaseResponse response = ResponseFactory.createSuccessResponse("Password reset instructions sent to your email");

        log.info("Password reset email sent to: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token");

        authenticationService.resetPassword(request.getResetToken(), request.getNewPassword());

        BaseResponse response = ResponseFactory.createSuccessResponse("Password reset successful");

        log.info("Password reset successful");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> verifyEmail(String token) {
        log.info("Email verification attempt");

        authenticationService.verifyEmail(token);

        BaseResponse response = ResponseFactory.createSuccessResponse("Email verified successfully");

        log.info("Email verified successfully");
        return ResponseEntity.ok(response);
    }
}
