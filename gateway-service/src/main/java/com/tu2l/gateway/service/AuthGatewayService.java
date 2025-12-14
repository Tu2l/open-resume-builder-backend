package com.tu2l.gateway.service;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AuthGatewayService {
    /**
     * Validate the given token locally by checking its signature and expiration.
     *
     * @param token JWT token string
     * @return true if the token is valid, false otherwise
     * @throws Exception if there is an error during validation
     */
    boolean validateToken(String token) throws Exception;

    /**
     * Validate the given token both locally and remotely by checking its signature,
     * expiration, and consulting an external authentication service.
     *
     * @param token JWT token string
     * @return true if the token is valid both locally and remotely, false otherwise
     * @throws Exception if there is an error during validation
     */
    boolean validateTokenInRemote(String token) throws Exception;

    /**
     * Enrich the given ServerHttpRequest with user information extracted from the token.
     *
     * @param request the original ServerHttpRequest
     * @param token   JWT token string
     * @return a new ServerHttpRequest enriched with user information
     * @throws Exception if there is an error during the enrichment process
     */
    ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest request, String token) throws Exception;
}
