package com.tu2l.user.controller;

import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.constants.AuthenticationMessages;
import com.tu2l.user.controller.api.AuthenticationApi;
import com.tu2l.user.model.request.*;
import com.tu2l.user.model.response.AuthResponse;
import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.utils.AuthResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RestController
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticationService authenticationService;
    private final AuthResponseBuilder authResponseBuilder;

    @Override
    public ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody NewUserRegisterRequest request) {
        var registeredUser = authenticationService.register(request);
        AuthResponse response = authResponseBuilder.buildAuthResponse(
                registeredUser,
                AuthenticationMessages.USER_REGISTERED_SUCCESS
        );

        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<@NonNull AuthResponse> authenticate(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        var loggedInUserEntity = authenticationService.authenticate(
                request.getUsernameOrEmail(),
                request.getPassword(),
                request.getRememberMe()
        );

        AuthResponse response = authResponseBuilder.buildAuthResponse(
                loggedInUserEntity,
                AuthenticationMessages.LOGIN_SUCCESS
        );

        log.info("Login successful for user: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<@NonNull AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        var refreshedUserEntity = authenticationService.refreshToken(
                request.getRefreshToken(),
                request.getUsername()
        );

        AuthResponse response = authResponseBuilder.buildAuthResponse(
                refreshedUserEntity,
                AuthenticationMessages.TOKEN_REFRESHED_SUCCESS
        );

        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> logout(String token) {
        var success = authenticationService.logout(token);
        log.info("Logout attempt: {}", success ? "successful" : "failed");
        return getResponse(success, AuthenticationMessages.LOGOUT_SUCCESS, AuthenticationMessages.LOGOUT_FAILED_INVALID_TOKEN);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> forgotPassword(ForgotPasswordRequest request) {
        var success = authenticationService.forgotPassword(request.getEmail());
        log.info("Forgot password attempt: {}", success ? "successful" : "failed");

        return getResponse(success, AuthenticationMessages.PASSWORD_RESET_EMAIL_SENT, AuthenticationMessages.PASSWORD_RESET_EMAIL_FAILED);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> resetPassword(ResetPasswordRequest request) {
        var success = authenticationService.resetPassword(request.getResetToken(), request.getNewPassword());
        log.info("Reset password attempt: {}", success ? "successful" : "failed");

        return getResponse(success, AuthenticationMessages.PASSWORD_RESET_SUCCESS, AuthenticationMessages.PASSWORD_RESET_FAILED);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> verifyEmail(String token) {
        var success = authenticationService.verifyEmail(token);
        log.info("Verify email attempt: {}", success ? "successful" : "failed");

        return getResponse(success, AuthenticationMessages.EMAIL_VERIFIED_SUCCESS, AuthenticationMessages.EMAIL_VERIFICATION_FAILED);
    }

    /**
     * Helper method to generate standardized API responses based on operation success.
     *
     * @param success        Indicates if the operation was successful
     * @param successMessage Message to return on success
     * @param failureMessage Message to return on failure
     * @return ResponseEntity with appropriate status and message
     */
    private @NonNull ResponseEntity<@NonNull BaseResponse> getResponse(boolean success, String successMessage, String failureMessage) {
        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseFactory.createErrorResponse(failureMessage));
        }
        return ResponseEntity.ok(ResponseFactory.createSuccessResponse(successMessage));
    }
}
