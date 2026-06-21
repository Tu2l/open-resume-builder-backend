package com.tu2l.user.service;

import com.tu2l.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin-only user operations: bulk listing of all users.
 * Individual user lookups and mutations live in {@link UserService},
 * where they are shared between self-service flows and internal service calls.
 */
public interface AdminUserService {

    /** Paginated list of all non-deleted users. */
    Page<UserEntity> getAllUsers(Pageable pageable);
}
