package com.tu2l.gateway.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.gateway.service.AuthGatewayService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthGatewayServiceImpl implements AuthGatewayService {
    private final JwtUtil jwtUtil;

    public AuthGatewayServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean validateToken(String token) throws JwtException, AuthenticationException {
        return !jwtUtil.isTokenExpired(token);
    }

    @Override
    public boolean validateTokenInRemote(String token) throws Exception {
        // TODO call remote auth service to validate token if needed
        return true;
    }

    @Override
    public ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest request, String token) throws AuthenticationException {
        Claims claims = jwtUtil.extractAllClaims(token);
        var tokenType = jwtUtil.extractClaim(claims, claim -> claim.get(CommonConstants.JwtClaims.TOKEN_TYPE, String.class));
        var email = jwtUtil.extractClaim(claims, claim -> claim.get(CommonConstants.JwtClaims.EMAIL, String.class));
        var role = jwtUtil.extractClaim(claims, claim -> claim.get(CommonConstants.JwtClaims.ROLE, String.class));

        if (!CommonConstants.Token.TOKEN_TYPE_BEARER.equals(tokenType) || StringUtil.isNullOrEmpty(email) || StringUtil.isNullOrEmpty(role)) {
            throw new AuthenticationException("Invalid token: missing user information");
        }

        log.info("Request enriched with user info: email={}, role={}", email, role);
        return request.mutate()
                .header(CommonConstants.Headers.X_USER_EMAIL, email)
                .header(CommonConstants.Headers.X_USER_ROLE, role)
                .build();
    }
}
