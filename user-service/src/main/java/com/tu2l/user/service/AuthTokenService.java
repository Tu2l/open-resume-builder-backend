package com.tu2l.user.service;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;
import io.jsonwebtoken.JwtException;

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
 * <li>{@link #refreshAccessToken(String, String)} — Refreshes an access token using a
 * refresh token.</li>
 * </ul>
 */
public interface AuthTokenService {

    /**
     * Generate JWT token for a user with specified token type
     *
     * @param user      the user entity for whom the token is generated
     * @param tokenType the type of token to generate (ACCESS, REFRESH,
     *                  PASSWORD_RESET,
     *                  EMAIL_VERIFICATION)
     * @return the generated JWT token as a String
     * @throws JwtException if token generation fails
     */
    String generateToken(UserEntity user, JwtTokenType tokenType) throws JwtException;

    /**
     * Validate a JWT token based on its type
     *
     * @param token     the JWT token to validate
     * @param tokenType the type of token to validate (ACCESS, REFRESH,
     *                  PASSWORD_RESET,
     *                  EMAIL_VERIFICATION)
     * @return true if the token is valid, false otherwise
     * @throws JwtException if token validation fails
     */
    boolean validateToken(String token, JwtTokenType tokenType) throws JwtException;

    /**
     * Refresh an access token using a refresh token
     *
     * @param refreshToken the refresh token used to generate a new access token
     * @param username     the username of the user
     * @param email        the email of the user
     * @param role         the role of the user
     * @return the new access token as a String
     * @throws JwtException if the refresh token is invalid or expired
     */
    String refreshAccessToken(String refreshToken, String username, String email, UserRole role) throws JwtException;

    /**
     * Get username from token
     *
     * @param token jwt token
     * @return username from token after verification against live database
     */
    String getUsername(String token);

    /**
     * Verify if the token has the specified role
     *
     * @param token jwt token
     * @param role  user role to verify
     * @return true if the token has the role, false otherwise
     */
    boolean verifyRole(String token, UserRole role);
}