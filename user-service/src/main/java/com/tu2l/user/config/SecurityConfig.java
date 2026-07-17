package com.tu2l.user.config;

import com.tu2l.user.filter.TrustedHeaderAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    TrustedHeaderAuthFilter trustedHeaderAuthFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Delegate CORS to the existing WebMvcConfigurer in BeansConfiguration.
                .cors(Customizer.withDefaults())
                // Gateway guards the perimeter; this service trusts the injected headers.
                // All request-level access is permitAll — RBAC is enforced per-method via @PreAuthorize,
                // and the resulting AccessDeniedException is translated to 403 by GlobalExceptionHandler.
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(trustedHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
