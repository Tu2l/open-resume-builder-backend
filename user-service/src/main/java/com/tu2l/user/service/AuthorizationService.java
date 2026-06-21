package com.tu2l.user.service;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.authorization.Permission;
import com.tu2l.user.exception.UserException;

import java.util.Set;

/**
 * Role-based access control (RBAC) operations: resolving and checking the
 * permissions granted to roles and users, and assigning roles.
 */
public interface AuthorizationService {

    /** Whether the given role is allowed to perform {@code action} on {@code resource}. */
    boolean hasPermission(UserRole role, String resource, String action);

    /** All roles defined in the system. */
    Set<UserRole> getAllRoles();

    /** All permissions defined in the system. */
    Set<Permission> getAllPermissions();

    /** The role currently assigned to the given user. */
    UserRole getUserRole(Long userId) throws UserException;

    /** Assigns {@code roleName} to the user and returns the new role. */
    UserRole assignRole(Long userId, String roleName) throws UserException;

    /** Permissions granted to the given role. */
    Set<Permission> getPermissionsForRole(UserRole role);

    /** Permissions granted to the given user (via their role). */
    Set<Permission> getPermissionsForUser(Long userId) throws UserException;
}
