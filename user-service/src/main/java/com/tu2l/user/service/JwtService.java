package com.tu2l.user.service;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;

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
 * <li>{@link #refreshAccessToken(String)} — Refreshes an access token using a
 * refresh token.</li>
 * </ul>
 */
public interface JwtService {

    /**
     * Generate JWT token for a user with specified token type
     * 
     * @param user      the user entity for whom the token is generated
     * @param tokenType the type of token to generate (ACCESS, REFRESH, PASSWORD_RESET,
     *                  EMAIL_VERIFICATION)
     * @return the generated JWT token as a String
     * @throws Exception if token generation fails
     */
    String generateToken(UserEntity user, JwtTokenType tokenType) throws Exception;

    /**
     * Validate a JWT token based on its type
     * 
     * @param token     the JWT token to validate
     * @param tokenType the type of token to validate (ACCESS, REFRESH, PASSWORD_RESET,
     *                  EMAIL_VERIFICATION)
     * @return true if the token is valid, false otherwise
     * @throws Exception if token validation fails
     */
    Boolean validateToken(String token, JwtTokenType tokenType) throws Exception;

    /**
     * Refresh an access token using a refresh token
     * 
     * @param refreshToken the refresh token used to generate a new access token
     * @return the new access token as a String
     * @throws Exception if the refresh token is invalid or expired 
     */
    String refreshAccessToken(String refreshToken) throws Exception;

    String getUsername(String token);

    Long getUserId(String token);

    Boolean verifyRole(String token, UserRole admin);
}