package com.tu2l.user.authorization;

import com.tu2l.common.model.states.UserRole;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Static, in-code mapping of {@link UserRole} to the {@link Permission}s it grants.
 * Kept in code (no DB table) to keep the RBAC model simple and dependency-free.
 */
public final class RolePermissions {

    private static final Map<UserRole, Set<Permission>> MATRIX = Map.of(
            UserRole.ADMIN, EnumSet.allOf(Permission.class),
            UserRole.MODERATOR, EnumSet.of(
                    Permission.USER_READ, Permission.USER_WRITE,
                    Permission.USER_READ_ALL, Permission.ROLE_READ,
                    Permission.PDF_GENERATE, Permission.PDF_READ),
            UserRole.USER, EnumSet.of(
                    Permission.USER_READ, Permission.USER_WRITE, Permission.USER_DELETE,
                    Permission.PDF_GENERATE, Permission.PDF_READ),
            UserRole.GUEST, EnumSet.of(Permission.PDF_READ)
    );

    private RolePermissions() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Returns the (unmodifiable) set of permissions granted to the given role. */
    public static Set<Permission> forRole(UserRole role) {
        return Collections.unmodifiableSet(MATRIX.getOrDefault(role, EnumSet.noneOf(Permission.class)));
    }

    /** Whether the given role is granted the given permission. */
    public static boolean roleHas(UserRole role, Permission permission) {
        return permission != null && forRole(role).contains(permission);
    }
}
