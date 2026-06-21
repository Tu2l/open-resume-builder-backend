package com.tu2l.user.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Records {@link AuditEvent}s. Each event is written in its own
 * {@link Propagation#REQUIRES_NEW} transaction so it persists even when the
 * surrounding business transaction rolls back (e.g. a failed login). Auditing
 * failures are logged and swallowed so they can never break the primary flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditEventType eventType, Long userId, String details) {
        try {
            HttpServletRequest request = currentRequest();
            AuditEvent event = AuditEvent.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .details(details)
                    .ipAddress(request != null ? clientIp(request) : null)
                    .userAgent(request != null ? truncate(request.getHeader("User-Agent")) : null)
                    .build();
            auditEventRepository.save(event);
        } catch (RuntimeException e) {
            log.error("Failed to record audit event {} for user {}", eventType, userId, e);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 512 ? value : value.substring(0, 512);
    }
}
