package com.tu2l.user.service;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.response.UserDTO;

import java.util.Optional;

/**
 * Self-service and internal user operations.
 * Admin-only bulk/cross-user lookups live in {@link AdminUserService}.
 */
public interface UserService {

    /**
     * Retrieves user details by their unique ID.
     * Used internally by other services (e.g. authorization flows).
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
     * Retrieves user with profile and account status eagerly loaded.
     * Use this when you need to access user's profile or account status outside the transaction.
     *
     * @param username the username of the user to retrieve
     * @return a UserEntity with profile and accountStatus eagerly loaded
     * @throws UserException if the user is not found
     */
    UserEntity getUserWithDetails(String username) throws UserException;

    /**
     * Retrieves user by email with profile and account status eagerly loaded.
     * Use this when you need to access user's profile or account status outside the transaction.
     *
     * @param email the email address of the user to retrieve
     * @return a UserEntity with profile and accountStatus eagerly loaded
     * @throws UserException if the user is not found
     */
    UserEntity getUserByEmailWithDetails(String email) throws UserException;

    /**
     * Retrieves user with ALL relationships (profile, accountStatus, credentials) eagerly loaded.
     * Use this for authentication/authorization flows that need to access credentials.
     *
     * @param username the username of the user to retrieve
     * @return a UserEntity with all relationships eagerly loaded
     * @throws UserException if the user is not found
     */
    UserEntity getUserWithCredentials(String username) throws UserException;

    /**
     * Retrieves user by email with ALL relationships (profile, accountStatus, credentials) eagerly loaded.
     * Use this for authentication/authorization flows that need to access credentials.
     *
     * @param email the email address of the user to retrieve
     * @return a UserEntity with all relationships eagerly loaded
     * @throws UserException if the user is not found
     */
    UserEntity getUserByEmailWithCredentials(String email) throws UserException;

    /**
     * Retrieves user by email with all relationships eagerly loaded, returning empty
     * rather than throwing when absent. Use this where a missing user must not be
     * distinguishable to the caller (e.g. forgot-password, to avoid account enumeration).
     *
     * @param email the email address of the user to retrieve
     * @return the user wrapped in an Optional, or empty if no such user exists
     */
    Optional<UserEntity> findByEmailWithCredentials(String email);

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
