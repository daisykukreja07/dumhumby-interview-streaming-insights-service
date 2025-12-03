package com.example.insights.controller;

import com.example.insights.dto.TokenRequest;
import com.example.insights.dto.TokenResponse;
import com.example.insights.security.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for token generation.
 * This is for testing/demonstration purposes only.
 * In production, use a proper authentication service.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates a JWT token for testing purposes.
     * 
     * POST /api/v1/auth/token
     * 
     * Request Body:
     * {
     *   "tenantId": "tenant123",
     *   "username": "user@example.com"
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tenantId": "tenant123",
     *   "username": "user@example.com"
     * }
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody TokenRequest request) {
        logger.info("Generating token for tenant: {}, username: {}", request.getTenantId(), request.getUsername());
        
        String token = jwtUtil.generateToken(request.getTenantId(), request.getUsername());
        
        TokenResponse response = new TokenResponse(
            token,
            request.getTenantId(),
            request.getUsername()
        );
        
        logger.info("Successfully generated token for tenant: {}", request.getTenantId());
        return ResponseEntity.ok(response);
    }
}

