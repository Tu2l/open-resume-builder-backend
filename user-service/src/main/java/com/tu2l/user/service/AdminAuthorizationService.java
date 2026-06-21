package com.tu2l.user.service;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.authorization.Permission;
import com.tu2l.user.exception.UserException;

import java.util.Set;

/**
 * Admin-only RBAC management operations: reading and mutating roles and permissions.
 * Self-service permission queries live in {@link AuthorizationService}.
 */
public interface AdminAuthorizationService {

    /** All roles defined in the system. */
    Set<UserRole> getAllRoles();

    /** All permissions defined in the system. */
    Set<Permission> getAllPermissions();

    /** The role currently assigned to the given user. */
    UserRole getUserRole(Long userId) throws UserException;

    /** Assigns {@code roleName} to the user and returns the new role. */
    UserRole assignRole(Long userId, String roleName) throws UserException;

    /** Permissions granted to the given user (via their role). */
    Set<Permission> getPermissionsForUser(Long userId) throws UserException;
}
