package com.tu2l.user.constants;

/**
 * Constants class containing authentication-related API response messages.
 * These are client-facing messages that need consistency across the application.
 * <p>
 * Note: Log messages are NOT included here as they are context-specific
 * and should remain inline for better code readability.
 */
public final class AuthenticationMessages {

    // API Success Response Messages
    public static final String USER_REGISTERED_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String TOKEN_REFRESHED_SUCCESS = "Token refreshed successfully";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successful";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset instructions sent to your email";
    public static final String EMAIL_VERIFIED_SUCCESS = "Email verified successfully";
    // API Error Response Messages
    public static final String LOGOUT_FAILED_INVALID_TOKEN = "Logout failed: Invalid token";
    public static final String PASSWORD_RESET_EMAIL_FAILED = "Failed to send password reset email";
    public static final String EMAIL_VERIFICATION_FAILED = "Email verification failed: Invalid or expired token";
    public static final String PASSWORD_RESET_FAILED = "Password reset failed: Invalid or expired token";

    private AuthenticationMessages() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}

