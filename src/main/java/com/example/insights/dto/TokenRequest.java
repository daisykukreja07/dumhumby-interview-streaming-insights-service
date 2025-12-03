package com.example.insights.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for generating a JWT.
 */
public class TokenRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Username is required")
    private String username;

    public TokenRequest() {
    }

    public TokenRequest(String tenantId, String username) {
        this.tenantId = tenantId;
        this.username = username;
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

