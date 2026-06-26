package com.tu2l.user.repository;

import com.tu2l.user.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    /**
     * Bulk-deletes credentials whose validity window has already elapsed. This is the
     * primary purge used by the scheduled cleanup; it bounds per-user credential growth
     * regardless of the {@code active} flag.
     *
     * @param cutoff delete rows with {@code expiresAt} strictly before this instant
     * @return number of rows removed
     */
    long deleteByExpiresAtBefore(LocalDateTime cutoff);

    /**
     * Bulk-deletes credentials that have been deactivated (e.g. rotated refresh tokens
     * past their reuse-detection window). Exposed for manual/administrative cleanup; the
     * scheduled job favours {@link #deleteByExpiresAtBefore} to preserve reuse detection.
     *
     * @return number of rows removed
     */
    long deleteByActiveFalse();
}
