package com.annadhata.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiAnalyticsReportResponse {
    private String executiveSummary;
    private double redistributionSuccessRate;
    private double totalRedistributedKg;
    private long totalCompletedDonations;
    private long totalExpiredDonations;
    private String peakDonationHours;
    private String highestSurplusCategory;
    private String highestDeficitCategory;
    private List<String> strategicInsights = new ArrayList<>();
    private List<String> actionableRecommendations = new ArrayList<>();
    private Map<String, Long> categoryDistribution;
    private Instant generatedAt = Instant.now();

    public AiAnalyticsReportResponse() {}

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public double getRedistributionSuccessRate() { return redistributionSuccessRate; }
    public void setRedistributionSuccessRate(double redistributionSuccessRate) { this.redistributionSuccessRate = redistributionSuccessRate; }

    public double getTotalRedistributedKg() { return totalRedistributedKg; }
    public void setTotalRedistributedKg(double totalRedistributedKg) { this.totalRedistributedKg = totalRedistributedKg; }

    public long getTotalCompletedDonations() { return totalCompletedDonations; }
    public void setTotalCompletedDonations(long totalCompletedDonations) { this.totalCompletedDonations = totalCompletedDonations; }

    public long getTotalExpiredDonations() { return totalExpiredDonations; }
    public void setTotalExpiredDonations(long totalExpiredDonations) { this.totalExpiredDonations = totalExpiredDonations; }

    public String getPeakDonationHours() { return peakDonationHours; }
    public void setPeakDonationHours(String peakDonationHours) { this.peakDonationHours = peakDonationHours; }

    public String getHighestSurplusCategory() { return highestSurplusCategory; }
    public void setHighestSurplusCategory(String highestSurplusCategory) { this.highestSurplusCategory = highestSurplusCategory; }

    public String getHighestDeficitCategory() { return highestDeficitCategory; }
    public void setHighestDeficitCategory(String highestDeficitCategory) { this.highestDeficitCategory = highestDeficitCategory; }

    public List<String> getStrategicInsights() { return strategicInsights; }
    public void setStrategicInsights(List<String> strategicInsights) { this.strategicInsights = strategicInsights; }

    public List<String> getActionableRecommendations() { return actionableRecommendations; }
    public void setActionableRecommendations(List<String> actionableRecommendations) { this.actionableRecommendations = actionableRecommendations; }

    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
