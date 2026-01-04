package com.tu2l.user.utils;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.user.entity.UserCredential;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.response.AuthResponse;
import org.springframework.stereotype.Component;

/**
 * Helper service for building authentication responses.
 * Reduces code duplication in authentication operations.
 */
@Component
public class AuthResponseBuilder {

    /**
     * Builds an AuthResponse from a UserEntity.
     * Extracts the most recent login token and refresh token.
     *
     * @param user    The user entity containing login information
     * @param message The success message for the response
     * @return Configured AuthResponse with tokens and metadata
     * @throws AuthenticationException if login token is not found
     */
    public AuthResponse buildAuthResponse(UserEntity user, String message) throws AuthenticationException {
        UserCredential userCredential = user.getLatestCredentials()
                .orElseThrow(() -> new AuthenticationException("Login token not found"));

        AuthResponse response = AuthResponse.builder()
                .accessToken(userCredential.getToken())
                .expiresIn(userCredential.getExpiresAt())
                .refreshToken(user.getTokenByType(JwtTokenType.REFRESH))
                .build();

        return ResponseFactory.configureResponse(response, message, ResponseProcessingStatus.SUCCESS);
    }

    /**
     * Builds an AuthResponse from a UserEntity with default success message.
     *
     * @param user The user entity containing login information
     * @return Configured AuthResponse with tokens and metadata
     * @throws AuthenticationException if login token is not found
     */
    public AuthResponse buildAuthResponse(UserEntity user) throws AuthenticationException {
        return buildAuthResponse(user, "Operation successful");
    }
}

