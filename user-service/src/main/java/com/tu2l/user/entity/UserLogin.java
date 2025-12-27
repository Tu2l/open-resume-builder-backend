package com.tu2l.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_logins", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user_id", columnList = "userId")
})
public class UserLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long expiresIn;

    @UpdateTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime loggedInAt;
}
