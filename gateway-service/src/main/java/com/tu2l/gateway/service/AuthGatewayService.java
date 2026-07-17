package com.tu2l.gateway.service;

import com.tu2l.common.exception.AuthenticationException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AuthGatewayService {

    boolean validateToken(String token) throws JwtException, AuthenticationException;

    ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest request, String token) throws AuthenticationException;
}
