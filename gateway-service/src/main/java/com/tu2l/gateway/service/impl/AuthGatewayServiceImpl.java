package com.tu2l.gateway.service.impl;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.gateway.service.AuthGatewayService;
import io.jsonwebtoken.Claims;
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
    public boolean validateToken(String token) throws Exception {
        return !jwtUtil.isTokenExpired(token);
    }

    @Override
    public boolean validateTokenInRemote(String token) throws Exception {
        // TODO call remote auth service to validate token if needed
        return true;
    }

    @Override
    public ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest request, String token) throws Exception {
        Claims claims = jwtUtil.extractAllClaims(token);
        String email = jwtUtil.extractClaim(claims, claim -> claim.get(CommonConstants.JwtClaims.EMAIL, String.class));
        String role = jwtUtil.extractClaim(claims, claim -> claim.get(CommonConstants.JwtClaims.ROLE, String.class));

        log.info("Request enriched with user info: email={}, role={}", email, role);
        return request.mutate()
                .header(CommonConstants.Headers.X_USER_EMAIL, email)
                .header(CommonConstants.Headers.X_USER_ROLE, role)
                .build();
    }
}
