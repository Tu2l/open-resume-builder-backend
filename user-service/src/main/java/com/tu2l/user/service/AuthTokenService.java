package com.tu2l.user.service;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;
import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;

/**
 * Service for handling JWT operations such as token generation, validation, and
 * refreshing.
 * This interface defines methods for creating JWT tokens for different
 * purposes,
 * validating them, and refreshing access tokens using refresh tokens.
 *
 * <ul>
 * <li>{@link #generateToken(UserEntity, JwtTokenType)} — Generates a JWT token
 * for a user.</li>
 * <li>{@link #validateToken(String, JwtTokenType)} — Validates a JWT token
 * based on its type.</li>
 * <li>{@link #refreshAccessToken(String, UserEntity)} — Refreshes an access token using a
 * refresh token.</li>
 * </ul>
 */
public interface AuthTokenService {

    /**
     * Generate JWT token for a user with specified token type.
     *
     * @param user      the user data for token claims
     * @param tokenType the type of token to generate (ACCESS, REFRESH, PASSWORD_RESET, EMAIL_VERIFICATION)
     * @return generated token value
     * @throws JwtException if token generation fails
     */
    String generateToken(UserEntity user, JwtTokenType tokenType) throws JwtException;

    /**
     * Validate a JWT token based on its type.
     */
    boolean validateToken(String token, JwtTokenType tokenType) throws JwtException;

    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken the refresh token used to generate a new access token
     * @param user         the user entity associated with the refresh token (claims source)
     * @return refreshed access token
     * @throws JwtException if the refresh token is invalid or expired
     */
    String refreshAccessToken(String refreshToken, UserEntity user) throws JwtException;

    /**
     * Get username from token.
     */
    String getUsername(String token);

    /**
     * Verify if the token has the specified role.
     */
    boolean verifyRole(String token, UserRole role);

    /**
     * Remaining lifetime in seconds for the token.
     */
    long getTokenRemainingTime(String token);

    /**
     * Issued-at timestamp for the token.
     */
    LocalDateTime issuedAt(String token);

    /**
     * Expires at timestamp for the token.
     */
    LocalDateTime expiresAt(String token);

    /**
     * Get token type from token.
     */
    JwtTokenType getTokenType(String token);
}