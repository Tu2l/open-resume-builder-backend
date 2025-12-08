package com.tu2l.gateway.util;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class WebPathUtil {
    private final AntPathMatcher matcher;

    public WebPathUtil(AntPathMatcher matcher) {
        this.matcher = matcher;
    }

    public boolean isPublicRoute(String path, List<String> publicRoutes) {
        if (publicRoutes == null || publicRoutes.isEmpty()) {
            return false;
        }
        return publicRoutes.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }
}
