package com.tu2l.common.util;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
public class JwtUtil {

    private final String secretKey;

    private final long accessTokenExpirationMinutes;

    private final long refreshTokenExpirationDays;

    private final String issuer;

    // --- Token Generation Methods ---

    /**
     * Generate access token for user authentication
     */
    public String generateAccessToken(String username, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JwtClaims.EMAIL, email);
        claims.put(CommonConstants.JwtClaims.TOKEN_TYPE, JwtTokenType.ACCESS.getValue());
        claims.put(CommonConstants.JwtClaims.ROLE, role);
        return createToken(claims, username, accessTokenExpirationMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Generate refresh token for obtaining new access tokens
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JwtClaims.TOKEN_TYPE, JwtTokenType.REFRESH.getValue());
        return createToken(claims, username, refreshTokenExpirationDays, ChronoUnit.DAYS);
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String username, String email) {
        Map<String, Object> claims = new HashMap<>(); // TODO update token claims for security
        claims.put(CommonConstants.JwtClaims.EMAIL, email);
        claims.put(CommonConstants.JwtClaims.TOKEN_TYPE, JwtTokenType.PASSWORD_RESET.getValue());
        return createToken(claims, username, 1, ChronoUnit.HOURS);
    }

    /**
     * Generate email verification token
     */
    public String generateEmailVerificationToken(String username, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JwtClaims.EMAIL, email);
        claims.put(CommonConstants.JwtClaims.TOKEN_TYPE, JwtTokenType.EMAIL_VERIFICATION.getValue());
        return createToken(claims, username, 24, ChronoUnit.HOURS);
    }

    /**
     * Create JWT token with custom claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, long duration, ChronoUnit unit) {
        Instant now = Instant.now();
        Instant expiration = now.plus(duration, unit);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .id(UUID.randomUUID().toString()) // Add unique JTI to prevent duplicate tokens
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // --- Token Extraction Methods ---

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get(CommonConstants.JwtClaims.EMAIL, String.class));
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(CommonConstants.JwtClaims.ROLE, String.class));
    }

    /**
     * Extract token type from token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CommonConstants.JwtClaims.TOKEN_TYPE, String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(Claims claims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // --- Token Validation Methods ---

    /**
     * Validate token against username
     */
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return CommonConstants.Token.TOKEN_TYPE_REFRESH.equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Validate password reset token
     */
    public boolean validatePasswordResetToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return CommonConstants.Token.TOKEN_TYPE_PASSWORD_RESET.equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Validate email verification token
     */
    public boolean validateEmailVerificationToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return CommonConstants.Token.TOKEN_TYPE_EMAIL_VERIFICATION.equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Get remaining time before token expires (in seconds)
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        long expirationTime = expiration.getTime();
        long currentTime = System.currentTimeMillis();
        return (expirationTime - currentTime) / 1000;
    }

    // --- Helper Methods ---

    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpirationMinutes * 60 * 1000;
    }

    /**
     * Get refresh token expiration time in milliseconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
    }

    public LocalDateTime issuedAt(String accessToken) {
        Date issuedAt = extractClaim(accessToken, Claims::getIssuedAt);
        return LocalDateTime.ofInstant(issuedAt.toInstant(), java.time.ZoneId.systemDefault());
    }

    public LocalDateTime expiresAt(String accessToken) {
        Date expiration = extractClaim(accessToken, Claims::getExpiration);
        return LocalDateTime.ofInstant(expiration.toInstant(), java.time.ZoneId.systemDefault());
    }
}
