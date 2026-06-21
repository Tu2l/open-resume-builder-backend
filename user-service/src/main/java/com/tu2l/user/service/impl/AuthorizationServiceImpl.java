package com.tu2l.user.service.impl;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.audit.AuditEventType;
import com.tu2l.user.audit.AuditService;
import com.tu2l.user.authorization.Permission;
import com.tu2l.user.authorization.RolePermissions;
import com.tu2l.user.config.CacheConfig;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.service.AdminAuthorizationService;
import com.tu2l.user.service.AuthorizationService;
import com.tu2l.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService, AdminAuthorizationService {

    private final UserService userService;
    private final AuditService auditService;

    @Override
    public boolean hasPermission(UserRole role, String resource, String action) {
        Permission permission = Permission.from(resource, action);
        return RolePermissions.roleHas(role, permission);
    }

    @Override
    public Set<UserRole> getAllRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @Override
    public Set<Permission> getAllPermissions() {
        return EnumSet.allOf(Permission.class);
    }

    @Override
    public UserRole getUserRole(Long userId) throws UserException {
        return userService.getUserById(userId).getRole();
    }

    @Override
    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public UserRole assignRole(Long userId, String roleName) throws UserException {
        UserRole newRole = parseRole(roleName);
        UserEntity user = userService.getUserById(userId);
        user.setRole(newRole);
        userService.saveUser(user);
        auditService.log(AuditEventType.ROLE_ASSIGNED, userId, "role=" + newRole);
        log.info("Assigned role {} to user {}", newRole, userId);
        return newRole;
    }

    @Override
    public Set<Permission> getPermissionsForRole(UserRole role) {
        return RolePermissions.forRole(role);
    }

    @Override
    public Set<Permission> getPermissionsForUser(Long userId) throws UserException {
        return RolePermissions.forRole(getUserRole(userId));
    }

    private UserRole parseRole(String roleName) throws UserException {
        if (roleName == null || roleName.isBlank()) {
            throw new UserException("Role name must not be empty");
        }
        // Tolerate a raw string or a JSON string body (e.g. "ADMIN").
        String normalized = roleName.trim().replaceAll("^\"|\"$", "").trim().toUpperCase();
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new UserException("Unknown role: " + roleName);
        }
    }
}
