package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    private RateLimitFilter filter(RateLimitProperties props) {
        return new RateLimitFilter(new FixedWindowRateLimiter(props), props);
    }

    private HttpServletRequest requestTo(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    @Test
    void filtersConfiguredPaths() {
        var props = new RateLimitProperties(true, 5, 60, List.of("/auth/authenticate", "/auth/register"), 300000);
        var filter = filter(props);

        assertThat(filter.shouldNotFilter(requestTo("/users/v1/auth/authenticate"))).isFalse();
        assertThat(filter.shouldNotFilter(requestTo("/users/v1/auth/register"))).isFalse();
    }

    @Test
    void doesNotFilterUnlistedPaths() {
        var props = new RateLimitProperties(true, 5, 60, List.of("/auth/authenticate"), 300000);
        assertThat(filter(props).shouldNotFilter(requestTo("/users/v1/me"))).isTrue();
    }

    @Test
    void doesNotFilterWhenDisabled() {
        var props = new RateLimitProperties(false, 5, 60, List.of("/auth/authenticate"), 300000);
        assertThat(filter(props).shouldNotFilter(requestTo("/users/v1/auth/authenticate"))).isTrue();
    }
}
