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
@Table(
        name = "users_v2",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_email", columnNames = "email")
        }
)
public class UserEntityV2 {
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
    private List<UserCredentials> credentials = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (isOAuthUser && password != null) {
            throw new IllegalStateException("OAuth users should not have a password set.");
        }

        profile.setUser(this);
        accountStatus.setUser(this);
        credentials.forEach(cred -> cred.setUser(this));
    }
}
