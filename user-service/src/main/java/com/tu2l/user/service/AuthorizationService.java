package com.tu2l.user.service;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.authorization.Permission;

import java.util.Set;

/**
 * Self-service RBAC queries available to any authenticated user.
 * Admin-only role and permission management lives in {@link AdminAuthorizationService}.
 */
public interface AuthorizationService {

    /** Whether the given role is allowed to perform {@code action} on {@code resource}. */
    boolean hasPermission(UserRole role, String resource, String action);

    /** Permissions granted to the given role. */
    Set<Permission> getPermissionsForRole(UserRole role);
}
