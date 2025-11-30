package com.tu2l.common.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

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
    public String generateAccessToken(Long userId, String username, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        return createToken(claims, username, accessTokenExpirationMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Generate refresh token for obtaining new access tokens
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", "refresh");
        return createToken(claims, username, refreshTokenExpirationDays, ChronoUnit.DAYS);
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(Long userId, String username, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("tokenType", "password_reset");
        return createToken(claims, username, 1, ChronoUnit.HOURS);
    }

    /**
     * Generate email verification token
     */
    public String generateEmailVerificationToken(Long userId, String username, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("tokenType", "email_verification");
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
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract token type from token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
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
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
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
    public boolean validateToken(String token, String username) throws Exception {
        final String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return "refresh".equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Validate password reset token
     */
    public boolean validatePasswordResetToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return "password_reset".equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Validate email verification token
     */
    public boolean validateEmailVerificationToken(String token) throws Exception {
        String tokenType = extractTokenType(token);
        return "email_verification".equals(tokenType) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) throws Exception {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Get remaining time before token expires (in seconds)
     */
    public long getTokenRemainingTime(String token) throws Exception {
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
}
