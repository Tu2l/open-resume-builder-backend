package com.tu2l.user.service;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.request.RegisterRequest;

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
 * <li>{@link #refreshToken(String)} — Issues a new authentication token using a
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
     */
    UserEntity register(RegisterRequest request);

    /**
     * Authenticates a user with credentials and optional session persistence.
     *
     * @param username   the username or email of the user
     * @param password   the user's password
     * @param rememberMe whether to create a persistent session
     * @return a UserEntity representing the authenticated user
     */
    UserEntity authenticate(String username, String password, boolean rememberMe);

    /**
     * Issues a new authentication token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return a UserEntity representing the refreshed authentication
     */
    UserEntity refreshToken(String refreshToken);

    /**
     * Invalidates an existing authentication token to end a user session.
     *
     * @param token the authentication token to invalidate
     */
    void logout(String token);

    /**
     * Initiates the password recovery flow for the specified email address.
     *
     * @param email the email address of the user
     * @return a UserEntity representing the recovery outcome
     */
    UserEntity forgotPassword(String email);

    /**
     * Resets the user password when provided with a valid reset token.
     *
     * @param token       the password reset token
     * @param newPassword the new password to set
     * @return a UserEntity representing the reset outcome
     */
    UserEntity resetPassword(String token, String newPassword);

    /**
     * Confirms a user’s email address using a verification token.
     *
     * @param verificationToken the email verification token
     * @return a UserEntity representing the verification outcome
     */
    UserEntity verifyEmail(String verificationToken);
}