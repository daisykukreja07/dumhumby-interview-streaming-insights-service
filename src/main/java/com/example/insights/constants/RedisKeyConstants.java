package com.example.insights.constants;

/**
 * Constants for Redis key construction.
 */
public final class RedisKeyConstants {
    
    private RedisKeyConstants() {
        // Prevent instantiation
    }
    
    public static final String KEY_PREFIX = "campaign";
    public static final String KEY_SEPARATOR = ":";
    
    /**
     * Builds a Redis key for campaign metrics.
     * Format: campaign:{campaignId}:{metricType}
     *
     * @param campaignId the campaign identifier
     * @param metricType the type of metric
     * @return the constructed Redis key
     */
    public static String buildKey(String campaignId, String metricType) {
        return KEY_PREFIX + KEY_SEPARATOR + campaignId + KEY_SEPARATOR + metricType;
    }
}

