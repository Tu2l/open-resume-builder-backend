package com.tu2l.user.entity;

import com.tu2l.common.model.states.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // Hashed password

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 20)
    private String phoneNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(length = 100)
    private String emailVerificationToken;

    @Column
    private LocalDateTime emailVerificationExpiry;

    @Column(length = 100)
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetExpiry;

    @Column(length = 255)
    private String refreshToken;

    @Column
    private LocalDateTime refreshTokenExpiry;

    @Column
    private LocalDateTime lastLoginAt;

    @Builder.Default
    @Column
    private Integer failedLoginAttempts = 0;

    @Column
    private LocalDateTime accountLockedUntil;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt; // For soft delete

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserLogin> userLogins = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (role == null) {
            role = UserRole.USER;
        }
        if (enabled == null) {
            enabled = true;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        if (userLogins == null) {
            userLogins = new ArrayList<>();
        }
        userLogins.forEach(login -> login.setUser(this));
    }

    public Integer incrementFailedLoginAttempts() {
        if (this.failedLoginAttempts == null) {
            this.failedLoginAttempts = 1;
        } else {
            this.failedLoginAttempts += 1;
        }
        return failedLoginAttempts;
    }

    // Helper methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public void lockAccount(int lockDurationMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null
                && passwordResetExpiry != null
                && passwordResetExpiry.isAfter(LocalDateTime.now());
    }

    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null
                && emailVerificationExpiry != null
                && emailVerificationExpiry.isAfter(LocalDateTime.now());
    }

    public boolean isRefreshTokenValid() {
        return refreshToken != null
                && refreshTokenExpiry != null
                && refreshTokenExpiry.isAfter(LocalDateTime.now());
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    public void clearSensitiveTokens() {
        this.passwordResetToken = null;
        this.passwordResetExpiry = null;
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }

    public UserLogin getMostRecentLogin() {
        if (userLogins == null || userLogins.isEmpty()) {
            return null;
        }
        return userLogins.stream()
                .max((login1, login2) -> login1.getLoggedInAt().compareTo(login2.getLoggedInAt()))
                .orElse(null);
    }

    public void addUserLogin(UserLogin login) {
        login.setUser(this);
        if (this.userLogins == null) {
            this.userLogins = new ArrayList<>();
        }
        this.userLogins.add(login);
    }
}
