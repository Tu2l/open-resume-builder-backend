package com.tu2l.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration. Declares the bearer-JWT security scheme so the
 * Swagger UI (served at {@code /users/swagger-ui.html}) can authorize requests.
 */
@Configuration
public class OpenAPIConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/api/users").description("Resume Builder Gateway"))
                .info(new Info()
                        .title("User Service API")
                        .description("User management, authentication and authorization service")
                        .version("v1")
                        .contact(new Contact().name("Resume Builder").email("support@resume-builder.app")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * Restores the {@code v} prefix on the version path segment of every generated path.
     * <p>
     * {@link com.tu2l.common.web.PrefixedSemanticApiVersionParser} strips the leading {@code v}
     * when parsing, so the framework's canonical version token is the bare number {@code 1}.
     * SpringDoc reconstructs paths from that token and emits {@code /1/auth/...} instead of
     * {@code /v1/auth/...}, which no longer matches the gateway's literal {@code /api/users/v1/**}
     * public-route patterns. This rewrites a leading numeric segment back to {@code /v<n>/...}.
     */
    @Bean
    public OpenApiCustomizer versionPrefixCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            var rewritten = new Paths();
            rewritten.setExtensions(openApi.getPaths().getExtensions());
            openApi.getPaths().forEach((path, item) ->
                    rewritten.addPathItem(path.replaceFirst("^/(\\d+)(?=/|$)", "/v$1"), item));
            openApi.setPaths(rewritten);
        };
    }
}
