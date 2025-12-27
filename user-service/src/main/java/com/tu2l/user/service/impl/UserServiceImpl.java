package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.response.UserDTO;
import com.tu2l.user.repository.UserRepository;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND_MSG = "User not found with username: ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommonUtil commonUtil;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, CommonUtil commonUtil,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.commonUtil = commonUtil;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return the UserEntity corresponding to the provided id
     * @throws UserException if the user is not found
     */
    @Override
    public UserEntity getUserById(Long id) throws UserException {
        log.info("Fetching user with id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + id));
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the UserEntity corresponding to the provided username
     * @throws UserException if the user is not found
     */
    @Override
    public UserEntity getUserByUsername(String username) throws UserException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UserException("User not found with username: " + username));
    }

    /**
     * Updates user profile information.
     *
     * @param userDTO the UserDTO containing updated user information
     * @return the updated UserEntity
     * @throws UserException if the user is not found or if the input is invalid
     */
    @Override
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

    /**
     * Deletes a user account by ID.
     *
     * @param username the unique identifier of the user to delete
     * @return a Boolean indicating the outcome of the deletion
     * @throws UserException if the user is not found
     */
    @Override
    public boolean deleteUser(String username) throws UserException {
        if (username == null) {
            throw new UserException("User ID must not be null");
        }
        // soft delete by setting deletedAt timestamp
        return userRepository.findUserByUsername(username).map(user -> {
            log.info("Deleting user with username: {}", username);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }).orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + username));
    }

    /**
     * Updates the user's password after validating the old password.
     *
     * @param username    the unique identifier of the user
     * @param oldPassword the current password of the user
     * @param newPassword the new password to set
     * @return the updated UserEntity
     * @throws UserException if the user is not found or if the old password does not
     *                       match
     */
    @Override
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
    public boolean existsByUsernameOrEmail(String username, String email) {
        log.info("Checking existence of user with username: {} or email: {}", username, email);
        return userRepository.existsByUsernameOrEmail(username, email);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UserException("User not found with email: " + email));
    }
}
