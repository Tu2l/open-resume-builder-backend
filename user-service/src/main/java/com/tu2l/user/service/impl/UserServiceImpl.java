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
    private static final String USER_NOT_FOUND_MSG = "User not found with id: ";

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
     * @throws Exception if the user is not found
     */
    @Override
    public UserEntity getUserById(Long id) throws Exception {
        log.info("Fetching user with id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new Exception(USER_NOT_FOUND_MSG + id));
    }

    /**
     * Updates user profile information.
     *
     * @param userDTO the UserDTO containing updated user information
     * @return the updated UserEntity
     * @throws Exception if the user is not found or if the input is invalid
     */
    @Override
    public UserEntity updateUser(UserDTO userDTO) throws Exception {
        if (userDTO == null || userDTO.getId() == null) {
            throw new UserException("UserDTO or User ID must not be null");
        }

        UserEntity updatedUser = userRepository.findById(userDTO.getId())
                .map(user -> userMapper.updateUserFromDTO(userDTO, user))
                .orElseThrow(() -> new Exception(USER_NOT_FOUND_MSG + userDTO.getId()));

        log.info("Updating user with id: {}", updatedUser.getId());
        return userRepository.save(updatedUser);
    }

    /**
     * Deletes a user account by ID.
     *
     * @param id the unique identifier of the user to delete
     * @return a Boolean indicating the outcome of the deletion
     * @throws Exception if the user is not found
     */
    @Override
    public Boolean deleteUser(Long id) throws Exception {
        if (id == null) {
            throw new UserException("User ID must not be null");
        }
        // soft delete by setting deletedAt timestamp
        return userRepository.findById(id).map(user -> {
            log.info("Deleting user with id: {}", id);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }).orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + id));
    }

    /**
     * Updates the user's password after validating the old password.
     *
     * @param id          the unique identifier of the user
     * @param oldPassword the current password of the user
     * @param newPassword the new password to set
     * @return the updated UserEntity
     * @throws Exception if the user is not found or if the old password does not
     *                   match
     */
    @Override
    public UserEntity updatePassword(Long id, String oldPassword, String newPassword) throws Exception {
        if (id == null) {
            throw new UserException("User ID must not be null");
        }
        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            throw new UserException("Old password and new password must not be null or empty");
        }

        String newPasswordPlainText = commonUtil.decodeBase64StringToString(newPassword);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MSG + id));

        if (!passwordEncoder.matches(commonUtil.decodeBase64StringToString(oldPassword), user.getPassword())) {
            throw new UserException("Old password does not match");
        }

        log.info("Updating password for user with id: {}", id);

        user.setPassword(passwordEncoder.encode(newPasswordPlainText));
        return userRepository.save(user);
    }
}
