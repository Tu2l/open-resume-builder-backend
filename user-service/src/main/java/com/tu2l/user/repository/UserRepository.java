package com.tu2l.user.repository;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /** True if at least one (non-deleted) user holds the given role. */
    boolean existsByRole(UserRole role);

    /**
     * Find a user by username or email (lightweight - no relationships loaded).
     *
     * @param username the username to search for
     * @param email    the email to search for
     * @return an Optional containing the found UserEntity, or empty if not found
     */
    Optional<UserEntity> findUserByUsernameOrEmail(String username, String email);

    /**
     * Find a user by email (lightweight - no relationships loaded).
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
     * @return true if a user exists with the given username or email, false otherwise
     */
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Find a user by username (lightweight - no relationships loaded).
     *
     * <p><b>Use this when:</b> You only need basic user fields and will access relationships
     * within the same transaction, or when you don't need relationships at all.</p>
     *
     * @param username the username to search for
     * @return an Optional containing the found UserEntity, or empty if not found
     */
    Optional<UserEntity> findUserByUsername(String username);

    // ========== Methods with Relationships Eagerly Fetched ==========
    // These methods are designed for intra-service usage where services like
    // AuthenticationService, AuthorizationService, etc., need to access related entities
    // OUTSIDE of the repository's transaction context. They prevent LazyInitializationException.

    /**
     * Find a user by username with profile and account status eagerly loaded.
     *
     * <p><b>Use this when:</b> You need to access user.getProfile() or user.getAccountStatus()
     * after the repository transaction has closed (e.g., in AuthenticationService).</p>
     *
     * <p><b>Example use cases:</b></p>
     * <ul>
     *   <li>Authentication flow checking account status</li>
     *   <li>Displaying user profile information</li>
     *   <li>User management operations</li>
     * </ul>
     *
     * @param username the username to search for
     * @return an Optional containing the found UserEntity with profile and status, or empty if not found
     */
    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.accountStatus " +
            "WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithDetails(@Param("username") String username);

    /**
     * Find a user by email with profile and account status eagerly loaded.
     *
     * <p><b>Use this when:</b> You need to access user.getProfile() or user.getAccountStatus()
     * after the repository transaction has closed.</p>
     *
     * <p><b>Example use cases:</b></p>
     * <ul>
     *   <li>Login authentication by email</li>
     *   <li>Password reset flows</li>
     *   <li>Email-based user lookup with profile info</li>
     * </ul>
     *
     * @param email the email to search for
     * @return an Optional containing the found UserEntity with profile and status, or empty if not found
     */
    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.accountStatus " +
            "WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithDetails(@Param("email") String email);

    /**
     * Find a user by username or email with profile and account status eagerly loaded.
     * Use this when you need user details including name, status, etc.
     *
     * @param username the username to search for
     * @param email    the email to search for
     * @return an Optional containing the found UserEntity with profile and status, or empty if not found
     */
    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.accountStatus " +
            "WHERE u.username = :username OR u.email = :email")
    Optional<UserEntity> findByUsernameOrEmailWithDetails(
            @Param("username") String username,
            @Param("email") String email
    );

    /**
     * Find a user by username with ALL relationships eagerly loaded (profile, status, credentials).
     *
     * <p><b>Use this when:</b> You need to access user.getCredentials() collection or iterate
     * over credentials after the repository transaction has closed.</p>
     *
     * <p><b>Example use cases:</b></p>
     * <ul>
     *   <li>Token refresh operations (checking refresh token validity)</li>
     *   <li>Logout operations (removing specific credentials)</li>
     *   <li>Password reset (checking password reset tokens, clearing all tokens)</li>
     *   <li>Session management</li>
     * </ul>
     *
     * <p><b>WARNING:</b> Can be expensive for users with many credentials.
     * Use {@link #findByUsernameWithDetails(String)} if you only need profile/status.</p>
     *
     * @param username the username to search for
     * @return an Optional containing the found UserEntity with all relationships, or empty if not found
     */
    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.accountStatus " +
            "LEFT JOIN FETCH u.credentials " +
            "WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithAll(@Param("username") String username);

    /**
     * Find a user by email with ALL relationships eagerly loaded (profile, status, credentials).
     *
     * <p><b>Use this when:</b> You need to access user.getCredentials() collection after
     * the repository transaction has closed, when looking up by email.</p>
     *
     * <p><b>Example use cases:</b></p>
     * <ul>
     *   <li>Email-based token operations</li>
     *   <li>Complete user data retrieval by email</li>
     * </ul>
     *
     * <p><b>WARNING:</b> Can be expensive for users with many credentials.
     * Use {@link #findByEmailWithDetails(String)} if you only need profile/status.</p>
     *
     * @param email the email to search for
     * @return an Optional containing the found UserEntity with all relationships, or empty if not found
     */
    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.accountStatus " +
            "LEFT JOIN FETCH u.credentials " +
            "WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithAll(@Param("email") String email);
}
