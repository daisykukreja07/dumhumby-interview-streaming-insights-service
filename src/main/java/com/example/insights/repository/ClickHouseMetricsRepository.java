package com.example.insights.repository;

import com.example.insights.multitenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class ClickHouseMetricsRepository {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseMetricsRepository.class);
    
    private static final String QUERY_WITH_TENANT = """
        SELECT value FROM ad_metrics
        WHERE tenant_id = ? AND campaign_id = ? AND metric_type = ?
        LIMIT 1
        """;
    
    private static final String QUERY_WITHOUT_TENANT = """
        SELECT value FROM ad_metrics
        WHERE campaign_id = ? AND metric_type = ?
        LIMIT 1
        """;

    private final String url;

    public ClickHouseMetricsRepository(
            @Value("${clickhouse.url:jdbc:clickhouse://localhost:8123/shopstream}") String url) {
        this.url = url;
        logger.info("ClickHouseMetricsRepository initialized with URL: {}", url);
    }

    public Long getMetric(String campaignId, String metricType) {
        String tenantId = TenantContext.getTenantId();
        logger.debug("Querying ClickHouse for tenant: {}, campaign: {}, metric: {}", 
                    tenantId, campaignId, metricType);
        
        try (Connection conn = DriverManager.getConnection(url)) {
            
            // Use tenant-aware query if tenant context is set
            if (tenantId != null) {
                return queryWithTenant(conn, tenantId, campaignId, metricType);
            } else {
                logger.warn("TenantId is null, querying without tenant filter");
                return queryWithoutTenant(conn, campaignId, metricType);
            }

        } catch (SQLException e) {
            logger.error("Database error while fetching metric for tenant: {}, campaign: {}, metric: {}. Error: {}", 
                        tenantId, campaignId, metricType, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching metric for tenant: {}, campaign: {}, metric: {}", 
                        tenantId, campaignId, metricType, e);
            return null;
        }
    }

    /**
     * Queries ClickHouse with tenant ID filter.
     */
    private Long queryWithTenant(Connection conn, String tenantId, String campaignId, String metricType) 
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(QUERY_WITH_TENANT)) {
            ps.setString(1, tenantId);
            ps.setString(2, campaignId);
            ps.setString(3, metricType);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long value = rs.getLong("value");
                    logger.debug("Found value {} in ClickHouse for tenant: {}, campaign: {}, metric: {}", 
                               value, tenantId, campaignId, metricType);
                    return value;
                }
            }
            
            logger.debug("No data found in ClickHouse for tenant: {}, campaign: {}, metric: {}", 
                        tenantId, campaignId, metricType);
            return null;
        }
    }

    /**
     * Queries ClickHouse without tenant ID filter (fallback for backward compatibility).
     */
    private Long queryWithoutTenant(Connection conn, String campaignId, String metricType) 
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(QUERY_WITHOUT_TENANT)) {
            ps.setString(1, campaignId);
            ps.setString(2, metricType);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long value = rs.getLong("value");
                    logger.debug("Found value {} in ClickHouse for campaign: {}, metric: {}", 
                               value, campaignId, metricType);
                    return value;
                }
            }
            
            logger.debug("No data found in ClickHouse for campaign: {}, metric: {}", campaignId, metricType);
            return null;
        }
    }
}
