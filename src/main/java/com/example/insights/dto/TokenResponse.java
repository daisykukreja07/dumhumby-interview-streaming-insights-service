package com.example.insights.dto;

/**
 * Response body containing a generated JWT and associated context.
 */
public class TokenResponse {

    private String token;
    private String tenantId;
    private String username;

    public TokenResponse() {
    }

    public TokenResponse(String token, String tenantId, String username) {
        this.token = token;
        this.tenantId = tenantId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

