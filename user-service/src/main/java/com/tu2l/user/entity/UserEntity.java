package com.tu2l.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    }
    
    // Helper methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
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
    
    public enum UserRole {
        USER,
        ADMIN,
        MODERATOR,
        GUEST
    }
}
