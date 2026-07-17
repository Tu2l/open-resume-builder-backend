package com.tu2l.user.web;

import com.tu2l.user.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the originating client IP for a request. {@code X-Forwarded-For} is only
 * honoured when the direct peer ({@link HttpServletRequest#getRemoteAddr()}) is a
 * configured trusted proxy (the gateway); otherwise the header is ignored to prevent
 * IP spoofing. Shared by the rate-limit filter and the audit service so both derive
 * the client IP identically.
 */
@Component
@RequiredArgsConstructor
public class ClientIpResolver {

    private final RateLimitProperties properties;

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (!properties.trustedProxies().isEmpty() && properties.trustedProxies().contains(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }
}
