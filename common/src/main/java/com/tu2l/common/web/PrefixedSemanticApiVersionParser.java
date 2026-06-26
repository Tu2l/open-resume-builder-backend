package com.tu2l.common.web;

import org.springframework.web.accept.ApiVersionParser;
import org.springframework.web.accept.SemanticApiVersionParser;

/**
 * {@link ApiVersionParser} that tolerates a leading {@code "v"}/{@code "V"} prefix on a version
 * token (e.g. {@code "v1"}, {@code "V2.1"}) before delegating to {@link SemanticApiVersionParser}.
 *
 * <p>This lets every service keep human-friendly {@code /v1/...} URL segments while the framework
 * still performs semantic-version comparison under the hood. Shared from {@code common} so the
 * versioning behaviour is identical across services and configured purely by wiring this parser
 * into each service's {@code ApiVersionConfigurer}.
 */
public class PrefixedSemanticApiVersionParser implements ApiVersionParser<SemanticApiVersionParser.Version> {

    private final SemanticApiVersionParser delegate = new SemanticApiVersionParser();

    @Override
    public SemanticApiVersionParser.Version parseVersion(String version) {
        String normalized = version;
        if (normalized != null && !normalized.isEmpty()) {
            char first = normalized.charAt(0);
            if (first == 'v' || first == 'V') {
                normalized = normalized.substring(1);
            }
        }
        return delegate.parseVersion(normalized);
    }
}
