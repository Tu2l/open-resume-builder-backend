package com.tu2l.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserLogin;
import com.tu2l.user.model.request.ForgotPasswordRequest;
import com.tu2l.user.model.request.LoginRequest;
import com.tu2l.user.model.request.RefreshTokenRequest;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.model.request.ResetPasswordRequest;
import com.tu2l.user.model.response.AuthResponse;
import com.tu2l.user.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication Controller - Handles authentication operations
 * Base path: /user/auth
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * POST /auth/register - Register a new user account
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) throws Exception {
        log.info("Registration request received for username: {}", request.getUsername());

        UserEntity registeredUser = authenticationService.register(request);
        UserLogin userLogin = registeredUser.getMostRecentLogin();

        AuthResponse response = new AuthResponse();
        response.setAccessToken(userLogin.getToken());
        response.setExpiresIn(userLogin.getExpiresIn());
        response.setRefreshToken(registeredUser.getRefreshToken());
        response.setMessage("User registered successfully");

        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login - Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws Exception {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        UserEntity loggedInUserEntity = authenticationService.authenticate(request.getUsernameOrEmail(),
                request.getPassword(), request.getRememberMe());
        UserLogin userLogin = loggedInUserEntity.getMostRecentLogin();

        AuthResponse response = new AuthResponse();
        response.setAccessToken(userLogin.getToken());
        response.setExpiresIn(userLogin.getExpiresIn());
        response.setRefreshToken(loggedInUserEntity.getRefreshToken());
        response.setMessage("Login successful");

        log.info("Login successful for user: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/refresh - Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws Exception {
        log.info("Token refresh request received");

        UserEntity refreshedUserEntity = authenticationService.refreshToken(request.getRefreshToken());

        AuthResponse response = new AuthResponse();
        response.setMessage("Token refreshed successfully");
        response.setAccessToken(refreshedUserEntity.getMostRecentLogin().getToken());
        response.setExpiresIn(refreshedUserEntity.getMostRecentLogin().getExpiresIn());
        response.setRefreshToken(refreshedUserEntity.getRefreshToken());

        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout - Logout user (invalidate tokens)
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse> logout(@RequestHeader("Authorization") String token) throws Exception {
        log.info("Logout request received");

        authenticationService.logout(token);

        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Logout successful");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Logout successful");
        return ResponseEntity.ok(response);
    }

    /**
     * forgot password, reset password, verify email are still incomplete
     */

    /**
     * POST /auth/forgot-password - Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request)
            throws Exception {
        log.info("Password reset request for email: {}", request.getEmail());

        authenticationService.forgotPassword(request.getEmail());

        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Password reset instructions sent to your email");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Password reset email sent to: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/reset-password - Reset password using reset token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
            throws Exception {
        log.info("Password reset attempt with token");

        authenticationService.resetPassword(request.getResetToken(), request.getNewPassword());

        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Password reset successful");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Password reset successful");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/verify-email - Verify email address (optional)
     */
    @PostMapping("/verify-email")
    public ResponseEntity<BaseResponse> verifyEmail(@RequestBody String verificationToken) throws Exception {
        log.info("Email verification attempt");

        authenticationService.verifyEmail(verificationToken);

        BaseResponse response = new BaseResponse() {
        };
        response.setMessage("Email verified successfully");
        response.setStatus(ResponseProcessingStatus.SUCCESS);

        log.info("Email verified successfully");
        return ResponseEntity.ok(response);
    }
}
