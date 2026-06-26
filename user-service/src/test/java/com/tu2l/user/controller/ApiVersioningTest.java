package com.tu2l.user.controller;

import com.tu2l.common.web.PrefixedSemanticApiVersionParser;
import com.tu2l.user.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.accept.DefaultApiVersionStrategy;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.PathApiVersionResolver;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Verifies the Spring Framework 7 native path-segment API versioning wiring used by the service,
 * driving {@link RequestMappingHandlerMapping} directly (no servlet container or database needed).
 *
 * <p>It mirrors {@code ApiVersioningConfiguration}: a {@code /{version}} path prefix plus a
 * {@link PathApiVersionResolver} at segment 0 and the shared {@link PrefixedSemanticApiVersionParser}.
 * The assertions prove (a) the type-level {@code version = "1+"} propagates to handler methods,
 * (b) the {@code /{version}} prefix is applied, and (c) an unsupported version is rejected.
 */
class ApiVersioningTest {

    private RequestMappingHandlerMapping mapping;
    private DefaultApiVersionStrategy strategy;

    @BeforeEach
    void setUp() {
        GenericWebApplicationContext ctx = new GenericWebApplicationContext(new MockServletContext());
        ctx.registerBean(AuthorizationService.class, () -> mock(AuthorizationService.class));
        ctx.registerBean(AuthorizationController.class);
        ctx.refresh();

        strategy = new DefaultApiVersionStrategy(
                List.of(new PathApiVersionResolver(0)),
                new PrefixedSemanticApiVersionParser(),
                false,   // versionRequired
                "1",     // defaultVersion
                true,    // detectSupportedVersions (mapped versions become supported)
                null,    // supportedVersionPredicate
                null);   // deprecationHandler

        mapping = new RequestMappingHandlerMapping();
        mapping.setApplicationContext(ctx);
        mapping.setPathPrefixes(Map.of("/{version}",
                HandlerTypePredicate.forBasePackage("com.tu2l.user.controller")));
        mapping.setApiVersionStrategy(strategy);
        mapping.afterPropertiesSet();
    }

    @Test
    void typeLevelVersionPropagatesAndPrefixIsApplied() {
        RequestMappingInfo checkMapping = mapping.getHandlerMethods().keySet().stream()
                .filter(info -> info.getPathPatternsCondition() != null
                        && info.getPathPatternsCondition().getPatternValues().stream()
                        .anyMatch(p -> p.endsWith("/authorize/check")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("authorize/check mapping not registered"));

        // /{version} prefix applied to the controller pattern.
        assertThat(checkMapping.getPathPatternsCondition().getPatternValues())
                .anyMatch(p -> p.contains("{version}"));
        // Type-level version="1+" propagated to the handler method (no per-method annotation).
        assertThat(checkMapping.getVersionCondition().getVersion()).contains("1");
    }

    @Test
    void resolvesV1FromPathSegment() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/authorize/check");
        req.addParameter("resource", "user");
        req.addParameter("action", "read");
        // DispatcherServlet parses and caches the RequestPath in real dispatch; do it here too.
        ServletRequestPathUtils.parseAndCache(req);

        HandlerExecutionChain chain = mapping.getHandler(req);
        assertThat(chain).as("/v1 should resolve to a handler").isNotNull();
    }

    @Test
    void unsupportedVersionIsRejected() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v2/authorize/check");
        // v1 is a supported (mapped) version; v2 is not.
        assertThatCode(() -> strategy.validateVersion(strategy.parseVersion("1"), req))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> strategy.validateVersion(strategy.parseVersion("2"), req))
                .isInstanceOf(InvalidApiVersionException.class);
    }
}
