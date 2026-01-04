package com.tu2l.user.entity;

import com.tu2l.common.model.JwtTokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_credentials",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_user_active", columnList = "user_id, active"),  // ✅ Composite
                @Index(name = "idx_token_type", columnList = "token_type, active")  // ✅ For filtering
        }
)
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false, name = "issued_at")
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "token_type", length = 20)
    private JwtTokenType tokenType;

    @Column(nullable = false, length = 100)
    private String issuer;

    @Column(nullable = false)
    private Boolean active;

    public boolean isTokenExpired() {
        return token != null
                && expiresAt != null
                && expiresAt.isAfter(LocalDateTime.now());
    }
}
