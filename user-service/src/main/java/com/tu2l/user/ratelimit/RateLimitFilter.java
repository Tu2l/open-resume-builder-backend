package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import com.tu2l.user.web.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applies {@link FixedWindowRateLimiter} to sensitive authentication endpoints
 * (login, register, refresh, forgot-password and reset-password), keyed by client IP.
 * Returns HTTP 429 with a standard error body when the limit is exceeded. Other paths
 * are not filtered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String TOO_MANY_REQUESTS_BODY =
            "{\"message\":\"Too many requests. Please try again later.\",\"status\":\"FAILURE\"}";

    private final FixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final ClientIpResolver clientIpResolver;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.enabled()) {
            return true;
        }
        String uri = request.getRequestURI();
        for (String suffix : properties.limitedPaths()) {
            if (uri.endsWith(suffix)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = clientIpResolver.resolve(request);
        String key = clientIp + ":" + request.getRequestURI();
        if (rateLimiter.tryAcquire(key)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.warn("Rate limit exceeded for {} on {}", clientIp, request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(TOO_MANY_REQUESTS_BODY);
    }
}
