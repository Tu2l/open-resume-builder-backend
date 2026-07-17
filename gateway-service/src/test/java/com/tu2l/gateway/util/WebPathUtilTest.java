package com.tu2l.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebPathUtilTest {

    private WebPathUtil pathUtil;

    @BeforeEach
    void setUp() {
        pathUtil = new WebPathUtil(new AntPathMatcher());
    }

    @Test
    void matchesPublicRoutePattern() {
        assertThat(pathUtil.isPublicRoute("/api/users/v1/auth/login", List.of("/api/users/v1/auth/**"))).isTrue();
    }

    @Test
    void protectedRouteDoesNotMatch() {
        assertThat(pathUtil.isPublicRoute("/api/users/v1/me", List.of("/api/users/v1/auth/**"))).isFalse();
    }

    @Test
    void nullPublicRoutesReturnsFalse() {
        assertThat(pathUtil.isPublicRoute("/api/users/v1/auth/login", null)).isFalse();
    }

    @Test
    void emptyPublicRoutesReturnsFalse() {
        assertThat(pathUtil.isPublicRoute("/api/users/v1/auth/login", List.of())).isFalse();
    }

    @Test
    void exactPathMatchWorks() {
        assertThat(pathUtil.isPublicRoute("/api/users/v1/api-docs", List.of("/api/users/v1/api-docs"))).isTrue();
    }
}
