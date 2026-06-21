package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.config.CacheConfig;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.service.AdminUserService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService, AdminUserService {
    private static final String USER_NOT_FOUND_MSG = "User not found with username: ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommonUtil commonUtil;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) throws UserException {
        log.info("Fetching user with id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + id));
    }

    @Override
    @Transactional(readOnly = true)
    // Caches only eager top-level fields; current callers (profile mapping, role
    // checks) never touch the lazy associations on a cache hit.
    @Cacheable(value = CacheConfig.USERS_CACHE, key = "#username")
    public UserEntity getUserByUsername(String username) throws UserException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UserException("User not found with username: " + username));
    }

    @Override
    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public UserEntity updateUser(UserDTO userDTO) throws UserException {
        if (userDTO == null || userDTO.getId() == null) {
            throw new UserException("UserDTO or User ID must not be null");
        }

        UserEntity updatedUser = userRepository.findById(userDTO.getId())
                .map(user -> userMapper.updateUserFromDTO(userDTO, user))
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + userDTO.getId()));

        log.info("Updating user with id: {}", updatedUser.getId());
        return userRepository.save(updatedUser);
    }

    @Override
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#username")
    public boolean deleteUser(String username) throws UserException {
        if (username == null) {
            throw new UserException("User ID must not be null");
        }
        // soft delete by setting deletedAt timestamp (hidden by @SQLRestriction thereafter)
        return userRepository.findUserByUsername(username).map(user -> {
            log.info("Deleting user with username: {}", username);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }).orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + username));
    }

    @Override
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#username")
    public UserEntity updatePassword(String username, String oldPassword, String newPassword) throws UserException {
        if (username == null) {
            throw new UserException("User ID must not be null");
        }
        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            throw new UserException("Old password and new password must not be null or empty");
        }

        String newPasswordPlainText = commonUtil.decodeBase64StringToString(newPassword);

        UserEntity user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + username));

        if (!passwordEncoder.matches(commonUtil.decodeBase64StringToString(oldPassword), user.getPassword())) {
            throw new UserException("Old password does not match");
        }

        log.info("Updating password for user with username: {}", username);

        user.setPassword(passwordEncoder.encode(newPasswordPlainText));
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameOrEmail(String username, String email) {
        log.info("Checking existence of user with username: {} or email: {}", username, email);
        return userRepository.existsByUsernameOrEmail(username, email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) throws UserException {
        log.info("Fetching user with email: {}", email);
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserWithDetails(String username) throws UserException {
        log.info("Fetching user with details for username: {}", username);
        return userRepository.findByUsernameWithDetails(username)
                .orElseThrow(() -> new UserException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByEmailWithDetails(String email) throws UserException {
        log.info("Fetching user with details for email: {}", email);
        return userRepository.findByEmailWithDetails(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserWithCredentials(String username) throws UserException {
        log.info("Fetching user with credentials for username: {}", username);
        return userRepository.findByUsernameWithAll(username)
                .orElseThrow(() -> new UserException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByEmailWithCredentials(String email) throws UserException {
        log.info("Fetching user with credentials for email: {}", email);
        return userRepository.findByEmailWithAll(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserEntity> getAllUsers(Pageable pageable) {
        log.info("Fetching users page: {}", pageable);
        return userRepository.findAll(pageable);
    }
}
