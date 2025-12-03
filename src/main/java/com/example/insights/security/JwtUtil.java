package com.example.insights.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token operations.
 * Handles token parsing, validation, and claim extraction.
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String TENANT_ID_CLAIM = "tenantId";

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        logger.info("JwtUtil initialized");
    }

    /**
     * Extracts the tenant ID from the JWT token.
     *
     * @param token the JWT token
     * @return the tenant ID, or null if not present or invalid
     */
    public String extractTenantId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tenantId = claims.get(TENANT_ID_CLAIM, String.class);
            
            if (tenantId == null) {
                logger.warn("JWT token does not contain tenantId claim");
                return null;
            }
            
            logger.debug("Extracted tenantId from JWT: {}", tenantId);
            return tenantId;
        } catch (Exception e) {
            logger.error("Error extracting tenantId from JWT token", e);
            return null;
        }
    }

    /**
     * Extracts the subject (username) from the JWT token.
     *
     * @param token the JWT token
     * @return the subject
     */
    public String extractSubject(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting subject from JWT token", e);
            return null;
        }
    }

    /**
     * Validates the JWT token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                logger.warn("JWT token is expired");
                return false;
            }
            
            // Check if tenantId claim exists
            if (claims.get(TENANT_ID_CLAIM, String.class) == null) {
                logger.warn("JWT token missing tenantId claim");
                return false;
            }
            
            logger.debug("JWT token is valid");
            return true;
        } catch (Exception e) {
            logger.error("JWT token validation failed", e);
            return false;
        }
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generates a JWT token for testing purposes.
     * In production, this should be done by your authentication service.
     *
     * @param tenantId the tenant ID
     * @param subject the subject (username)
     * @return the JWT token
     */
    public String generateToken(String tenantId, String subject) {
        return Jwts.builder()
                .subject(subject)
                .claim(TENANT_ID_CLAIM, tenantId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(secretKey)
                .compact();
    }
}

