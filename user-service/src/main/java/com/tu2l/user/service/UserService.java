package com.tu2l.user.service;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.response.UserDTO;

/**
 * Defines core user management operations including retrieval, profile updates,
 * account removal, and password changes within the user service layer.
 *
 * <p>This service provides comprehensive user management functionality including:</p>
 * <ul>
 * <li>{@link #getUserById(Long)} — Retrieves user details by their unique ID.</li>
 * <li>{@link #getUserByUsername(String)} — Retrieves user details by username.</li>
 * <li>{@link #getUserByEmail(String)} — Retrieves user details by email.</li>
 * <li>{@link #updateUser(UserDTO)} — Updates user profile information.</li>
 * <li>{@link #deleteUser(String)} — Deletes a user account by username.</li>
 * <li>{@link #updatePassword(String, String, String)} — Changes the user's password.</li>
 * <li>{@link #existsByUsernameOrEmail(String, String)} — Checks if a user exists.</li>
 * <li>{@link #saveUser(UserEntity)} — Persists a new or existing user entity.</li>
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
     * Retrieves user details by their username.
     *
     * @param username the username of the user to retrieve
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
     * Deletes a user account by username.
     *
     * @param username the username of the user to delete
     * @return true if the deletion was successful, false otherwise
     * @throws UserException if the user is not found or deletion fails
     */
    boolean deleteUser(String username) throws UserException;

    /**
     * Changes the user's password given the old and new passwords.
     *
     * @param username    the username of the user whose password will be changed
     * @param oldPassword the current password of the user for verification
     * @param newPassword the new password to set
     * @return the updated UserEntity with the new password
     * @throws UserException if the user is not found or if the old password does not match
     */
    UserEntity updatePassword(String username, String oldPassword, String newPassword) throws UserException;

    /**
     * Checks if a user exists by username or email.
     *
     * <p>This method is typically used during user registration to prevent
     * duplicate accounts with the same username or email address.</p>
     *
     * @param username the username to check for existence
     * @param email    the email address to check for existence
     * @return true if a user exists with the given username or email, false otherwise
     */
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Retrieves user details by their email address.
     *
     * @param email the email address of the user to retrieve
     * @return a UserEntity containing user details
     * @throws UserException if the user is not found
     */
    UserEntity getUserByEmail(String email) throws UserException;

    /**
     * Persists a new or existing user entity to the database.
     *
     * <p>This method can be used to save a new user or update an existing user's
     * information. If the user entity has an ID, it will update the existing record;
     * otherwise, it will create a new user record.</p>
     *
     * @param user the UserEntity to save or update
     * @return the saved UserEntity with updated information (e.g., generated ID)
     */
    UserEntity saveUser(UserEntity user);
}
