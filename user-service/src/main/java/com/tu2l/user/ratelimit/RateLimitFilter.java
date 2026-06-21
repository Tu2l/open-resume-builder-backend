package com.tu2l.user.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applies {@link FixedWindowRateLimiter} to sensitive authentication endpoints
 * (login and password-reset request), keyed by client IP. Returns HTTP 429 with a
 * standard error body when the limit is exceeded. Other paths are not filtered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String[] LIMITED_SUFFIXES = {"/auth/authenticate", "/auth/forgot-password"};

    private static final String TOO_MANY_REQUESTS_BODY =
            "{\"message\":\"Too many requests. Please try again later.\",\"status\":\"FAILURE\"}";

    private final FixedWindowRateLimiter rateLimiter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String suffix : LIMITED_SUFFIXES) {
            if (uri.endsWith(suffix)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientIp(request) + ":" + request.getRequestURI();
        if (rateLimiter.tryAcquire(key)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.warn("Rate limit exceeded for {} on {}", clientIp(request), request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(TOO_MANY_REQUESTS_BODY);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
