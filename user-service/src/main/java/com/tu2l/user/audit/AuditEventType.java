package com.tu2l.user.audit;

/**
 * Types of security-relevant events recorded by {@link AuditService}.
 */
public enum AuditEventType {
    USER_REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCOUNT_ENABLED,
    ACCOUNT_DISABLED,
    VERIFICATION_RESENT,
    LOGOUT,
    TOKEN_REFRESHED,
    TOKEN_REUSE_DETECTED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    EMAIL_VERIFIED,
    ROLE_ASSIGNED,
    PROFILE_UPDATED
}
