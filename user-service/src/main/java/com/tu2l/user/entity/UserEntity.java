package com.tu2l.user.entity;

import com.tu2l.common.model.JwtTokenType;
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
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile = new UserProfile();

    @Builder.Default
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAccountStatus accountStatus = new UserAccountStatus();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCredential> credentials = new ArrayList<>();

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
        credentials.forEach(cred -> cred.setUser(this));
    }


    public void addUserCredential(UserCredential credential) {
        credential.setUser(this);
        this.credentials.add(credential);
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
