package com.example.insights.security;

import com.example.insights.multitenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Filter that intercepts HTTP requests to extract and validate JWT tokens.
 * Extracts tenant ID from the JWT and sets it in the TenantContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // Extract tenant ID from JWT
                String tenantId = jwtUtil.extractTenantId(jwt);
                String subject = jwtUtil.extractSubject(jwt);
                
                if (tenantId != null && subject != null) {
                    // Set tenant context
                    TenantContext.setTenantId(tenantId);
                    logger.debug("Set tenant context for request: tenantId={}, subject={}", tenantId, subject);
                    
                    // Set Spring Security context
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(subject, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("Authentication set in SecurityContext for user: {}", subject);
                } else {
                    logger.warn("Invalid JWT token: missing tenantId or subject");
                }
            } else {
                logger.debug("No valid JWT token found in request");
            }
            
            filterChain.doFilter(request, response);
            
        } finally {
            // Always clear tenant context after request processing
            TenantContext.clear();
            logger.debug("Tenant context cleared after request processing");
        }
    }

    /**
     * Extracts JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            logger.debug("Extracted JWT token from Authorization header");
            return token;
        }
        
        return null;
    }
}

