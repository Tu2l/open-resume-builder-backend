package com.tu2l.user.utils;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.factory.ResponseFactory;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.response.AuthResponse;
import com.tu2l.user.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper service for building authentication responses.
 * Reduces code duplication in authentication operations.
 */
@Component
@RequiredArgsConstructor
public class AuthResponseBuilder {

    private final AuthTokenService authTokenService;

    /**
     * Builds an AuthResponse from a UserEntity.
     * <p>
     * The persisted {@link com.tu2l.user.entity.UserCredential} only holds token
     * <em>hashes</em>, so the raw JWTs are read from the entity's transient
     * {@code plainAccessToken}/{@code plainRefreshToken} fields (populated by the
     * authentication service for the current operation).
     *
     * @param user    The user entity containing the current operation's tokens
     * @param message The success message for the response
     * @return Configured AuthResponse with tokens and metadata
     * @throws AuthenticationException if no access token is present for this operation
     */
    public AuthResponse buildAuthResponse(UserEntity user, String message) throws AuthenticationException {
        String accessToken = user.getPlainAccessToken();
        if (accessToken == null) {
            throw new AuthenticationException("Login token not found");
        }

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(user.getPlainRefreshToken())
                .expiresIn(authTokenService.expiresAt(accessToken))
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
