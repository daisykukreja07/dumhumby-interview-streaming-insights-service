package com.example.insights.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local storage for the current tenant ID.
 * This class manages the tenant context for each request thread.
 */
public class TenantContext {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Sets the tenant ID for the current thread.
     *
     * @param tenantId the tenant identifier
     */
    public static void setTenantId(String tenantId) {
        logger.debug("Setting tenant context: {}", tenantId);
        currentTenant.set(tenantId);
    }

    /**
     * Gets the tenant ID for the current thread.
     *
     * @return the tenant identifier, or null if not set
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * Clears the tenant context for the current thread.
     * Should be called after request processing is complete.
     */
    public static void clear() {
        logger.debug("Clearing tenant context: {}", currentTenant.get());
        currentTenant.remove();
    }

    /**
     * Checks if a tenant context is set for the current thread.
     *
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean isSet() {
        return currentTenant.get() != null;
    }
}

