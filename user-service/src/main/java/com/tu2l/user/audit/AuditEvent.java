package com.tu2l.user.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable record of a security-relevant event (login, lockout, password reset,
 * role change, etc.) for audit trails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "audit_events",
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_event_type", columnList = "event_type"),
                @Index(name = "idx_audit_created_at", columnList = "created_at")
        }
)
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "event_type", length = 40)
    private AuditEventType eventType;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 512)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
}
