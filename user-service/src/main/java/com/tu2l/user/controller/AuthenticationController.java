package com.tu2l.user.controller;

import com.tu2l.common.constant.CommonConstants;
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

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthenticationController extends BaseController implements AuthenticationApi {

    private final AuthenticationService authenticationService;
    private final AuthResponseBuilder authResponseBuilder;

    @Override
    public ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody NewUserRegisterRequest request) {
        var registeredUser = authenticationService.register(request);
        var response = authResponseBuilder.buildAuthResponse(registeredUser, AuthenticationMessages.USER_REGISTERED_SUCCESS);
        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<@NonNull AuthResponse> authenticate(LoginRequest request) {
        log.info("Login attempt received");
        var loggedInUserEntity = authenticationService.authenticate(
                request.getEmail(), request.getPassword(), request.getRememberMe());
        var response = authResponseBuilder.buildAuthResponse(loggedInUserEntity, AuthenticationMessages.LOGIN_SUCCESS);
        log.info("Login successful");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        var refreshedUserEntity = authenticationService.refreshToken(request.getRefreshToken());
        var response = authResponseBuilder.buildAuthResponse(refreshedUserEntity, AuthenticationMessages.TOKEN_REFRESHED_SUCCESS);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<@NonNull BaseResponse> logout(String authorizationHeader) {
        // The Authorization header arrives with the scheme prefix ("Bearer <jwt>"); the
        // service hashes/parses the bare JWT, so strip the prefix before delegating.
        var success = authenticationService.logout(stripBearerPrefix(authorizationHeader));
        log.info("Logout attempt: {}", success ? "successful" : "failed");
        return getResponse(success, AuthenticationMessages.LOGOUT_SUCCESS, AuthenticationMessages.LOGOUT_FAILED_INVALID_TOKEN);
    }

    /** Strip a leading case-insensitive {@code "Bearer "} scheme if present; otherwise return as-is. */
    private static String stripBearerPrefix(String token) {
        if (token != null && token.regionMatches(true, 0, CommonConstants.Token.BEARER_PREFIX, 0,
                CommonConstants.Token.BEARER_PREFIX_LENGTH)) {
            return token.substring(CommonConstants.Token.BEARER_PREFIX_LENGTH);
        }
        return token;
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

    @Override
    public ResponseEntity<@NonNull BaseResponse> resendVerification(ResendVerificationRequest request) {
        authenticationService.resendVerification(request.getEmail());
        log.info("Resend verification requested");
        return success(AuthenticationMessages.VERIFICATION_EMAIL_SENT);
    }
}
