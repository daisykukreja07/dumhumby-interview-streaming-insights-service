package com.example.insights.repository;

import com.example.insights.constants.RedisKeyConstants;
import com.example.insights.multitenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisMetricsRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisMetricsRepository.class);

    private final StringRedisTemplate redisTemplate;

    public RedisMetricsRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        logger.info("RedisMetricsRepository initialized");
    }

    public Long getMetric(String campaignId, String metricType) {
        String tenantId = TenantContext.getTenantId();
        String key = buildTenantAwareKey(tenantId, campaignId, metricType);
        logger.debug("Attempting to retrieve metric from Redis with key: {} for tenant: {}", key, tenantId);
        
        try {
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                logger.debug("No value found in Redis for key: {}", key);
                return null;
            }
            
            Long result = Long.parseLong(value);
            logger.debug("Successfully retrieved value {} from Redis for key: {}", result, key);
            return result;
            
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in Redis for key: {}. Error: {}", key, e.getMessage());
            return null;
        } catch (RedisConnectionFailureException e) {
            logger.error("Redis connection failure while retrieving key: {}. Error: {}", key, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving metric from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Builds a tenant-aware Redis key.
     * Format: tenant:{tenantId}:campaign:{campaignId}:{metricType}
     *
     * @param tenantId the tenant identifier
     * @param campaignId the campaign identifier
     * @param metricType the metric type
     * @return the tenant-aware Redis key
     */
    private String buildTenantAwareKey(String tenantId, String campaignId, String metricType) {
        if (tenantId == null) {
            logger.warn("TenantId is null, using default key format");
            return RedisKeyConstants.buildKey(campaignId, metricType);
        }
        return "tenant:" + tenantId + ":" + RedisKeyConstants.buildKey(campaignId, metricType);
    }
}
