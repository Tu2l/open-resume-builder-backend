package com.tu2l.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "user_account_status")
public class UserAccountStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "accountStatus")
    private UserEntity user;

    @Column(nullable = false, name = "enabled")
    private boolean enabled = true;

    @Column(nullable = false, name = "email_verified")
    private boolean emailVerified;

    @Column(nullable = false, name = "phone_verified")
    private boolean phoneVerified;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public int incrementFailedLoginAttempts() {
        return ++failedLoginAttempts;
    }

    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * True when a lock was set but its window has already elapsed. Distinct from
     * {@link #isAccountLocked()} (which is false both when never locked and when expired).
     */
    public boolean isLockExpired() {
        return accountLockedUntil != null && !accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public void lockAccount(int lockDurationMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    /**
     * Clears an expired lock and resets the failed-attempt counter so the next single
     * failure doesn't immediately re-lock the account.
     */
    public void clearExpiredLock() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }
}
