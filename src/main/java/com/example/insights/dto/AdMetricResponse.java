package com.example.insights.dto;

/**
 * Simple DTO representing a single metric value for a campaign.
 */
public class AdMetricResponse {

    private String campaignId;
    private long value;

    public AdMetricResponse() {
    }

    public AdMetricResponse(String campaignId, long value) {
        this.campaignId = campaignId;
        this.value = value;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
