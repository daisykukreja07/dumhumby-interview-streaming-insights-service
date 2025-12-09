package com.example.insights.repository;

import com.example.insights.constants.RedisKeyConstants;
import com.example.insights.multitenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisMetricsRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisMetricsRepository.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;

    public RedisMetricsRepository(StringRedisTemplate redisTemplate,
                                  @Value("${redis.cache.ttl:PT1H}") Duration cacheTtl) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = cacheTtl;
        logger.info("RedisMetricsRepository initialized with cache TTL: {}", cacheTtl);
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
     * Saves a metric value to Redis cache with TTL.
     * This is used to cache frequently accessed metrics from ClickHouse.
     * Cache entries expire after the configured TTL to prevent stale data.
     *
     * @param campaignId the campaign identifier
     * @param metricType the metric type
     * @param value the metric value to cache
     */
    public void saveMetric(String campaignId, String metricType, Long value) {
        String tenantId = TenantContext.getTenantId();
        String key = buildTenantAwareKey(tenantId, campaignId, metricType);
        logger.debug("Caching metric in Redis with key: {} for tenant: {}, value: {}, TTL: {}", 
                    key, tenantId, value, cacheTtl);
        
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(value), cacheTtl);
            logger.debug("Successfully cached metric in Redis for key: {} with TTL: {}", key, cacheTtl);
        } catch (RedisConnectionFailureException e) {
            logger.error("Redis connection failure while caching key: {}. Error: {}", key, e.getMessage());
            // Don't throw - cache failures shouldn't break the API
        } catch (Exception e) {
            logger.error("Unexpected error caching metric in Redis for key: {}", key, e);
            // Don't throw - cache failures shouldn't break the API
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
