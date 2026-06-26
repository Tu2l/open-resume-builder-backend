package com.tu2l.pdf.config;

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
 * <p>Behind the gateway and {@code context-path: /pdf}, requests reach this service as
 * {@code /v1/...}, so the version lives at path-segment index {@code 0}. A global {@code /{version}}
 * path prefix lets controller patterns consume that segment, and {@link PrefixedSemanticApiVersionParser}
 * strips the leading {@code v} so {@code "v1"} is compared semantically against {@code version = "1+"}
 * declared on the controllers.
 *
 * <p>Resolution is lenient: a missing version falls back to {@code 1}; an unknown/unsupported
 * version yields HTTP 400.
 */
@Configuration
public class ApiVersioningConfiguration implements WebMvcConfigurer {

    private static final String CONTROLLER_BASE_PACKAGE = "com.tu2l.pdf.controller";
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
        // Scoped to our controllers so springdoc's /v3/api-docs handlers are left untouched.
        configurer.addPathPrefix("/{version}",
                HandlerTypePredicate.forBasePackage(CONTROLLER_BASE_PACKAGE));
    }
}
