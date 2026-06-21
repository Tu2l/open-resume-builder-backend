package com.tu2l.pdf.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI pdfServiceOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/api/pdf").description("Resume Builder Gateway"))
                .info(new Info()
                        .title("PDF Service API")
                        .description("PDF generation and retrieval service")
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
}
