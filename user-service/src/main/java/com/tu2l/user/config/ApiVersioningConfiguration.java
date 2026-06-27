package com.tu2l.user.config;

import com.tu2l.common.web.PrefixedSemanticApiVersionParser;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enables Spring Framework 7 native API versioning using a path segment.
 *
 * <p>Requests reach this service (behind the gateway and {@code context-path: /users}) as
 * {@code /v1/...}, so the version lives at path-segment index {@code 0}. A global {@code /{version}}
 * path prefix lets controller patterns consume that segment, and {@link PrefixedSemanticApiVersionParser}
 * strips the leading {@code v} so {@code "v1"} is compared semantically against {@code version = "1+"}
 * declared on the controllers.
 *
 * <p>Resolution is lenient: a missing version falls back to {@code 1}; an unknown/unsupported
 * version yields HTTP 400.
 *
 * <p><b>Constraint</b>: {@code usePathSegment(0)} makes the version strategy treat segment 0 of
 * <em>every</em> request through {@code RequestMappingHandlerMapping} (including SpringDoc) as a
 * version candidate — it is parsed and validated regardless of which handler matches. A segment
 * that is not a registered version yields HTTP 400. So infrastructure endpoints living on this
 * handler mapping must sit under a <em>supported</em> version segment: SpringDoc's api-docs path
 * is therefore {@code /v1/api-docs} (segment {@code v1} → version {@code 1}, which is supported),
 * not {@code /v3/api-docs} ({@code 3} unsupported) or {@code /openapi} (not a version at all).
 * Actuator endpoints are served by {@code WebMvcEndpointHandlerMapping}, a separate handler mapping
 * that does not inherit the version strategy, so {@code /actuator/**} is never affected.
 */
@Configuration
public class ApiVersioningConfiguration implements WebMvcConfigurer {

    private static final String CONTROLLER_BASE_PACKAGE = "com.tu2l.user.controller";
    private static final int VERSION_PATH_SEGMENT_INDEX = 0;
    private static final String DEFAULT_VERSION = "1";

    @Override
    public void configureApiVersioning(@NonNull ApiVersionConfigurer configurer) {
        configurer.usePathSegment(VERSION_PATH_SEGMENT_INDEX)
                .setVersionParser(new PrefixedSemanticApiVersionParser())
                .setDefaultVersion(DEFAULT_VERSION)
                .setVersionRequired(false);
    }

    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        // Scoped to our controllers only — SpringDoc and actuator handlers do not get the prefix.
        configurer.addPathPrefix("/{version}",
                HandlerTypePredicate.forBasePackage(CONTROLLER_BASE_PACKAGE));
    }
}
