package com.tu2l.gateway.service;

import com.tu2l.common.exception.AuthenticationException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AuthGatewayService {
    /**
     * Validate the given token locally by checking its signature and expiration.
     *
     * @param token JWT token string
     * @return true if the token is valid, false otherwise
     * @throws JwtException            if there is an error during validation
     * @throws AuthenticationException if authentication fails
     */
    boolean validateToken(String token) throws JwtException, AuthenticationException;

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
     * @throws AuthenticationException if there is an error during the enrichment process
     */
    ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest request, String token) throws AuthenticationException;
}
