package com.example.insights.service;

import com.example.insights.constants.MetricType;
import com.example.insights.exception.CampaignNotFoundException;
import com.example.insights.repository.RedisMetricsRepository;
import com.example.insights.repository.ClickHouseMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdInsightsService {

    private static final Logger logger = LoggerFactory.getLogger(AdInsightsService.class);

    private final RedisMetricsRepository redisRepo;
    private final ClickHouseMetricsRepository clickhouseRepo;

    public AdInsightsService(RedisMetricsRepository redisRepo,
                             ClickHouseMetricsRepository clickhouseRepo) {
        this.redisRepo = redisRepo;
        this.clickhouseRepo = clickhouseRepo;
        logger.info("AdInsightsService initialized");
    }

    public long getClicks(String campaignId) {
        validateCampaignId(campaignId);
        logger.debug("Fetching clicks for campaign: {}", campaignId);
        return fetchMetric(MetricType.CLICKS, campaignId);
    }

    public long getImpressions(String campaignId) {
        validateCampaignId(campaignId);
        logger.debug("Fetching impressions for campaign: {}", campaignId);
        return fetchMetric(MetricType.IMPRESSIONS, campaignId);
    }

    public long getClickToBasket(String campaignId) {
        validateCampaignId(campaignId);
        logger.debug("Fetching click-to-basket for campaign: {}", campaignId);
        return fetchMetric(MetricType.ADD_TO_CART, campaignId);
    }

    private long fetchMetric(String metricType, String campaignId) {
        logger.debug("Fetching metric {} for campaign {}", metricType, campaignId);
        
        // 1. Check Redis cache first (cache-aside pattern)
        Long cachedValue = redisRepo.getMetric(campaignId, metricType);
        if (cachedValue != null) {
            logger.debug("Cache hit: Found metric {} in Redis for campaign {}: {}", metricType, campaignId, cachedValue);
            return cachedValue;
        }
        
        logger.debug("Cache miss: Metric {} not found in Redis for campaign {}, querying ClickHouse", metricType, campaignId);

        // 2. Cache miss - query ClickHouse (source of truth, written by Apache Flink)
        Long chValue = clickhouseRepo.getMetric(campaignId, metricType);
        if (chValue == null) {
            logger.warn("Campaign {} not found for metric {} in ClickHouse", campaignId, metricType);
            throw new CampaignNotFoundException("Campaign " + campaignId + " not found.");
        }

        logger.debug("Found metric {} in ClickHouse for campaign {}: {}", metricType, campaignId, chValue);

        // 3. Populate Redis cache with the result for future requests
        // Cache only the final, most frequently accessed metrics
        redisRepo.saveMetric(campaignId, metricType, chValue);
        logger.debug("Cached metric {} for campaign {} in Redis: {}", metricType, campaignId, chValue);

        return chValue;
    }

    private void validateCampaignId(String campaignId) {
        if (!StringUtils.hasText(campaignId)) {
            logger.error("Invalid campaign ID: empty or null");
            throw new IllegalArgumentException("Campaign ID cannot be null or empty");
        }
    }
}
