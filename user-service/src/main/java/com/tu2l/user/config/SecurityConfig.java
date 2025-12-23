package com.tu2l.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal Spring Security Configuration
 *
 * Spring Security is included ONLY for BCryptPasswordEncoder (password hashing).
 * All authentication and authorization is handled by the API Gateway.
 *
 * This configuration disables all Spring Security protections and permits all requests.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF - not needed for stateless REST API
            .csrf(csrf -> csrf.disable())

            // Permit ALL requests - Gateway handles authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Disable form login
            .formLogin(form -> form.disable())

            // Disable HTTP Basic auth
            .httpBasic(basic -> basic.disable())

            // Disable logout
            .logout(logout -> logout.disable());

        return http.build();
    }
}

