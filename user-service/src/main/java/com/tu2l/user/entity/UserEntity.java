package com.tu2l.user.entity;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"profile", "accountStatus", "credentials", "plainAccessToken", "plainRefreshToken"})
@EqualsAndHashCode(exclude = {"profile", "accountStatus", "credentials", "plainAccessToken", "plainRefreshToken"})
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_email", columnNames = "email")
        }
)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "email", length = 100)
    private String email;

    @Column(nullable = false, name = "username", length = 50)
    private String username;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role", length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false, name = "oauth_user")
    private boolean isOAuthUser;

    @Column(name = "password", length = 255)
    private String password;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", unique = true, nullable = false)
    private UserProfile profile = null;

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_status_id", unique = true, nullable = false)
    private UserAccountStatus accountStatus = null;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCredential> credentials = new ArrayList<>();

    /**
     * Raw (un-hashed) access/refresh tokens for the current operation. Not persisted
     * ({@link Transient}) — only stored hashes live in {@link UserCredential}. These
     * carry the real JWTs back to the auth response after token hashing.
     */
    @Transient
    private String plainAccessToken;

    @Transient
    private String plainRefreshToken;

    @PrePersist
    void onCreate() {
        if (isOAuthUser && password != null) {
            throw new IllegalStateException("OAuth users should not have a password set.");
        }

        if (profile == null) {
            profile = new UserProfile();
        }
        if (accountStatus == null) {
            accountStatus = new UserAccountStatus();
        }
        if (credentials == null) {
            credentials = new ArrayList<>();
        }

        profile.setUser(this);
        accountStatus.setUser(this);
    }

    @PreUpdate
    void onUpdate() {
        if (isOAuthUser && password != null) {
            throw new IllegalStateException("OAuth users should not have a password set.");
        }
        if (profile != null && !Objects.equals(profile.getUser(), this)) {
            profile.setUser(this);
        }
        if (accountStatus != null && !Objects.equals(accountStatus.getUser(), this)) {
            accountStatus.setUser(this);
        }
    }

    public void addUserCredential(UserCredential credential) {
        if (credential != null) {
            credential.setUser(this);
            this.credentials.add(credential);
        }
    }

    public Optional<UserCredential> getLatestCredentials() {
        return credentials.stream()
                .max((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()));
    }

    public UserCredential getCredentialsByType(JwtTokenType type) {
        return credentials.stream()
                .filter(cred -> cred.getTokenType() == type)
                .findFirst()
                .orElse(null);
    }

    public UserCredential getCredentialByTokenTypeAndToken(JwtTokenType type, String token) {
        return credentials.stream()
                .filter(cred -> cred.getTokenType() == type && cred.getToken().equals(token))
                .findFirst()
                .orElse(null);
    }

    public boolean removeCredentialByToken(String token) {
        return credentials.removeIf(cred -> cred.getToken().equals(token));
    }

    public void clearSensitiveTokens() {
        credentials.clear();
    }

    public String getTokenByType(JwtTokenType jwtTokenType) {
        UserCredential credential = getCredentialsByType(jwtTokenType);
        return credential != null ? credential.getToken() : null;
    }
}
