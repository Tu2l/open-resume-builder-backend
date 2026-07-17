package com.tu2l.user.authorization;

/**
 * Fine-grained permissions used for role-based access control (RBAC).
 * <p>
 * Names follow the {@code RESOURCE_ACTION} convention so a (resource, action)
 * pair from an authorization check can be resolved to a permission via
 * {@link #from(String, String)}.
 */
public enum Permission {
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    USER_READ_ALL,
    USER_WRITE_ALL,
    USER_DELETE_ALL,
    ROLE_READ,
    ROLE_ASSIGN,
    PDF_GENERATE,
    PDF_READ;

    /**
     * Resolves a permission from a resource + action pair, e.g. ("user", "read")
     * → {@link #USER_READ}.
     *
     * @return the matching permission, or {@code null} if none exists
     */
    public static Permission from(String resource, String action) {
        if (resource == null || action == null) {
            return null;
        }
        try {
            return Permission.valueOf((resource + "_" + action).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
