package com.tu2l.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu2l.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * Find a user by username or email.
     *
     * @param username the username to search for
     * @param email    the email to search for
     * @return an Optional containing the found UserEntity, or empty if not found
     */
    Optional<UserEntity> findUserByUsernameOrEmail(String username, String email);

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the found UserEntity, or empty if not found
     */
    Optional<UserEntity> findUserByEmail(String email);

    /**
     * Check if a user exists by username or email.
     *
     * @param username the username to check
     * @param email    the email to check
     * @return true if a user exists with the given username or email, false
     *         otherwise
     */
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the found UserEntity, or empty if not found
     */
    Optional<UserEntity> findUserByUsername(String username);
}
