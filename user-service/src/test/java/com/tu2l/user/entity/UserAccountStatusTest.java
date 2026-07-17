package com.tu2l.user.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountStatusTest {

    @Test
    void isAccountLocked_trueOnlyWhileLockInFuture() {
        UserAccountStatus status = new UserAccountStatus();
        status.lockAccount(15);

        assertThat(status.isAccountLocked()).isTrue();
        assertThat(status.isLockExpired()).isFalse();
    }

    @Test
    void isLockExpired_trueWhenLockTimestampInPast() {
        UserAccountStatus status = new UserAccountStatus();
        status.setAccountLockedUntil(LocalDateTime.now().minusMinutes(1));
        status.setFailedLoginAttempts(5);

        assertThat(status.isAccountLocked()).isFalse();
        assertThat(status.isLockExpired()).isTrue();
    }

    @Test
    void isLockExpired_falseWhenNeverLocked() {
        UserAccountStatus status = new UserAccountStatus();

        assertThat(status.isAccountLocked()).isFalse();
        assertThat(status.isLockExpired()).isFalse();
    }

    @Test
    void clearExpiredLock_resetsTimestampAndCounter() {
        UserAccountStatus status = new UserAccountStatus();
        status.setAccountLockedUntil(LocalDateTime.now().minusMinutes(1));
        status.setFailedLoginAttempts(5);

        status.clearExpiredLock();

        assertThat(status.getAccountLockedUntil()).isNull();
        assertThat(status.getFailedLoginAttempts()).isZero();
    }

    @Test
    void lockAccount_setsTimestampRoughlyDurationAhead() {
        UserAccountStatus status = new UserAccountStatus();

        status.lockAccount(15);

        assertThat(status.getAccountLockedUntil())
                .isAfter(LocalDateTime.now().plusMinutes(14))
                .isBefore(LocalDateTime.now().plusMinutes(16));
    }
}
