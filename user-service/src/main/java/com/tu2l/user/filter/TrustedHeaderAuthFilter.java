package com.tu2l.user.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.states.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Converts gateway-injected identity headers into a Spring Security {@link Authentication}.
 * <p>
 * The gateway validates the JWT and injects {@code X-User-Username}, {@code X-User-Email},
 * and {@code X-User-Role} for every protected route before forwarding to this service.
 * This filter reads those trusted headers and populates the {@link SecurityContextHolder}
 * so that {@code @PreAuthorize} can evaluate role expressions.
 * <p>
 * For public routes (e.g. {@code /v1/auth/**}) the headers are absent; the filter sets
 * an anonymous authentication so protected methods are still correctly denied.
 */
@Component
public class TrustedHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var role = request.getHeader(CommonConstants.Headers.X_USER_ROLE);
        var username = request.getHeader(CommonConstants.Headers.X_USER_USERNAME);

        if (role != null && username != null) {
            // Normalise unknown roles to GUEST so @PreAuthorize("hasRole('ADMIN')") rejects them.
            var authority = isKnownRole(role) ? "ROLE_" + role : "ROLE_GUEST";
            var auth = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isKnownRole(String role) {
        try {
            UserRole.valueOf(role);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
