package com.tu2l.user.service;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.response.UserDTO;

/**
 * Defines core user management operations including retrieval, profile updates,
 * account removal, and password changes within the user service layer.
 *
 * <ul>
 * <li>{@link #getUserById(Long)} — Retrieves user details by their unique
 * ID.</li>
 * <li>{@link #updateUser(UserDTO)} — Updates user profile information.</li>
 * <li>{@link #deleteUser(Long)} — Deletes a user account by ID.</li>
 * <li>{@link #updatePassword(Long, String, String)} — Changes the user's
 * password given the old and new passwords.</li>
 * </ul>
 */
public interface UserService {
    /**
     * Retrieves user details by their unique ID.
     *
     * @param id the unique identifier of the user
     * @return a UserEntity containing user details
     * @throws UserException if the user is not found
     */
    UserEntity getUserById(Long id) throws UserException;

    /**
     * UserEntity getUserByUsername(String username) throws UserException;
     *
     * @param username the username of the user
     * @return a UserEntity containing user details
     * @throws UserException if the user is not found
     */
    UserEntity getUserByUsername(String username) throws UserException;

    /**
     * Updates user profile information.
     *
     * @param userDTO the UserDTO containing updated user information
     * @return the updated UserEntity
     * @throws UserException if the user is not found or if the input is invalid
     */
    UserEntity updateUser(UserDTO userDTO) throws UserException;

    /**
     * Deletes a user account by ID.
     *
     * @param username the unique identifier of the user to delete
     * @return a Boolean indicating the outcome of the deletion
     * @throws UserException if the user is not found
     */
    boolean deleteUser(String username) throws UserException;

    /**
     * Changes the user's password given the old and new passwords.
     *
     * @param username    the unique identifier of the user
     * @param oldPassword the current password of the user
     * @param newPassword the new password to set
     * @return the updated UserEntity
     * @throws UserException if the user is not found or if the old password does not
     *                       match
     */
    UserEntity updatePassword(String username, String oldPassword, String newPassword) throws UserException;

    /**
     * Check if a user exists by username or email.
     *
     * @param username the username to check
     * @param email    the email to check
     * @return true if a user exists with the given username or email, false
     * otherwise
     */
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Retrieves user details by their email.
     *
     * @param email the email of the user
     * @return a UserEntity containing user details
     */
    UserEntity getUserByEmail(String email);
}
