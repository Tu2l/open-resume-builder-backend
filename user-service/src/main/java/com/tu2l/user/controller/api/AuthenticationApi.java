package com.tu2l.user.controller.api;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.*;
import com.tu2l.user.model.response.AuthResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public interface AuthenticationApi {
    // register user
    @PostMapping("/register")
    ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody final RegisterRequest request) throws UserException;

    // authenticate user
    @PostMapping("/authenticate")
    ResponseEntity<@NonNull AuthResponse> authenticate(@Valid @RequestBody final LoginRequest request) throws UserException, AuthenticationException;

    // refresh token
    @PostMapping("/refresh-token")
    ResponseEntity<@NonNull AuthResponse> refreshToken(@Valid @RequestBody final RefreshTokenRequest request) throws JwtException, AuthenticationException;

    // invalidate token
    @PostMapping("/logout")
    ResponseEntity<@NonNull BaseResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws JwtException, AuthenticationException;

    // forgot password
    @PostMapping("/forgot-password")
    ResponseEntity<@NonNull BaseResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws JwtException, AuthenticationException;

    // update password
    @PostMapping("/reset-password")
    ResponseEntity<@NonNull BaseResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) throws JwtException, UserException, AuthenticationException;

    // verify email
    @PostMapping("/verify-email")
    ResponseEntity<@NonNull BaseResponse> verifyEmail(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws JwtException, AuthenticationException;
}

