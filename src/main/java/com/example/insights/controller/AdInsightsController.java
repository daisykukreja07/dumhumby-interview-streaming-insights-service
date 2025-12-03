package com.example.insights.controller;

import com.example.insights.dto.AdMetricResponse;
import com.example.insights.service.AdInsightsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ad")
@Validated
public class AdInsightsController {

    private static final Logger logger = LoggerFactory.getLogger(AdInsightsController.class);
    private static final String CAMPAIGN_ID_PATTERN = "^[a-zA-Z0-9_-]+$";
    private static final String CAMPAIGN_ID_VALIDATION_MESSAGE = "Campaign ID must contain only alphanumeric characters, hyphens, and underscores";

    private final AdInsightsService service;

    public AdInsightsController(AdInsightsService service) {
        this.service = service;
        logger.info("AdInsightsController initialized");
    }

    @GetMapping("/{campaignId}/clicks")
    public ResponseEntity<AdMetricResponse> getClicks(
            @PathVariable 
            @NotBlank(message = "Campaign ID cannot be blank")
            @Pattern(regexp = CAMPAIGN_ID_PATTERN, message = CAMPAIGN_ID_VALIDATION_MESSAGE)
            String campaignId) {
        
        logger.info("Received request to fetch clicks for campaign: {}", campaignId);
        long clicks = service.getClicks(campaignId);
        logger.info("Successfully fetched clicks for campaign: {}, count: {}", campaignId, clicks);
        return ResponseEntity.ok(new AdMetricResponse(campaignId, clicks));
    }

    @GetMapping("/{campaignId}/impressions")
    public ResponseEntity<AdMetricResponse> getImpressions(
            @PathVariable 
            @NotBlank(message = "Campaign ID cannot be blank")
            @Pattern(regexp = CAMPAIGN_ID_PATTERN, message = CAMPAIGN_ID_VALIDATION_MESSAGE)
            String campaignId) {
        
        logger.info("Received request to fetch impressions for campaign: {}", campaignId);
        long impressions = service.getImpressions(campaignId);
        logger.info("Successfully fetched impressions for campaign: {}, count: {}", campaignId, impressions);
        return ResponseEntity.ok(new AdMetricResponse(campaignId, impressions));
    }

    @GetMapping("/{campaignId}/clickToBasket")
    public ResponseEntity<AdMetricResponse> getClickToBasket(
            @PathVariable 
            @NotBlank(message = "Campaign ID cannot be blank")
            @Pattern(regexp = CAMPAIGN_ID_PATTERN, message = CAMPAIGN_ID_VALIDATION_MESSAGE)
            String campaignId) {
        
        logger.info("Received request to fetch click-to-basket for campaign: {}", campaignId);
        long clickToBasket = service.getClickToBasket(campaignId);
        logger.info("Successfully fetched click-to-basket for campaign: {}, count: {}", campaignId, clickToBasket);
        return ResponseEntity.ok(new AdMetricResponse(campaignId, clickToBasket));
    }
}
