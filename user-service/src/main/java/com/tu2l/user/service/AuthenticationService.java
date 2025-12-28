package com.tu2l.user.service;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import io.jsonwebtoken.JwtException;

/**
 * Defines the contract for user authentication workflows, including account
 * registration,
 * credential-based login, token management, and recovery operations.
 *
 * <ul>
 * <li>{@link #register(RegisterRequest)} — Creates a new user account based on
 * the supplied registration data.</li>
 * <li>{@link #authenticate(String, String, boolean)} — Authenticates a user
 * with credentials and optional session persistence.</li>
 * <li>{@link #refreshToken(String, String)} — Issues a new authentication token using a
 * valid refresh token.</li>
 * <li>{@link #logout(String)} — Invalidates an existing authentication token to
 * end a user session.</li>
 * <li>{@link #forgotPassword(String)} — Initiates the password recovery flow
 * for the specified email address.</li>
 * <li>{@link #resetPassword(String, String)} — Resets the user password when
 * provided with a valid reset token.</li>
 * <li>{@link #verifyEmail(String)} — Confirms a user’s email address using a
 * verification token.</li>
 * </ul>
 */
public interface AuthenticationService {
    /**
     * Registers a new user account.
     *
     * @param request the registration request containing user details
     * @return a UserEntity representing the newly registered user
     * @throws UserException if registration fails (e.g., username/email already exists)
     */
    UserEntity register(RegisterRequest request) throws UserException;

    /**
     * Authenticates a user with credentials and optional session persistence.
     *
     * @param usernameOrEmail the usernameOrEmail or email of the user
     * @param password        the user's password
     * @param rememberMe      whether to create a persistent session
     * @return a UserEntity representing the authenticated user
     * @throws UserException           if user not found or other user-related issues
     * @throws AuthenticationException if authentication fails (e.g., invalid credentials)
     */
    UserEntity authenticate(String usernameOrEmail, String password, boolean rememberMe) throws UserException, AuthenticationException;

    /**
     * Issues a new authentication token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @param username     the username of the user
     * @return an UserEntity representing the refreshed authentication
     * @throws JwtException            if the refresh token is invalid or expired
     * @throws AuthenticationException if token refresh fails
     * @throws UserException           if user is not valid
     */
    UserEntity refreshToken(String refreshToken, String username) throws JwtException, AuthenticationException, UserException;

    /**
     * Invalidates an existing authentication token to end a user session.
     *
     * @param token the authentication token to invalidate
     * @throws AuthenticationException if logout fails (e.g., token not found)
     * @throws JwtException            if the token is invalid
     */
    void logout(String token) throws JwtException, AuthenticationException;

    /**
     * Initiates the password recovery flow for the specified email address.
     *
     * @param email the email address of the user
     * @return a UserEntity representing the recovery outcome
     * @throws AuthenticationException if the email is not associated with any user
     */
    UserEntity forgotPassword(String email) throws JwtException, AuthenticationException;

    /**
     * Resets the user password when provided with a valid reset token.
     *
     * @param passwordResetToken the password reset token
     * @param newPassword        the new password to set
     * @return a UserEntity representing the reset outcome
     * @throws JwtException            if the reset token is invalid or expired
     * @throws AuthenticationException if password reset fails
     * @throws UserException           if user is not valid
     */
    UserEntity resetPassword(String passwordResetToken, String newPassword) throws JwtException, UserException, AuthenticationException;

    /**
     * Confirms a user’s email address using a verification token.
     *
     * @param verificationToken the email verification token
     * @return a UserEntity representing the verification outcome
     * @throws JwtException            if the token is invalid or expired
     * @throws AuthenticationException if email verification fails
     */
    UserEntity verifyEmail(String verificationToken) throws JwtException, AuthenticationException;
}