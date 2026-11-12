package com.annadhata.dto;

import java.util.ArrayList;
import java.util.List;

public class ReceiverDashboardResponse {
    private long availableNearbyDonations;
    private long activeClaimsCount;
    private long pendingPickupsCount;
    private long completedPickupsCount;
    private double totalQuantityReceivedKg;
    private double estimatedMealsDistributed;
    private double trustScore;
    private String trustTier;
    private List<DonationResponse> recommendedDonations = new ArrayList<>();
    private List<ClaimResponse> activeClaims = new ArrayList<>();
    private List<NeedResponse> currentNeeds = new ArrayList<>();

    public ReceiverDashboardResponse() {}

    public long getAvailableNearbyDonations() { return availableNearbyDonations; }
    public void setAvailableNearbyDonations(long availableNearbyDonations) { this.availableNearbyDonations = availableNearbyDonations; }

    public long getActiveClaimsCount() { return activeClaimsCount; }
    public void setActiveClaimsCount(long activeClaimsCount) { this.activeClaimsCount = activeClaimsCount; }

    public long getPendingPickupsCount() { return pendingPickupsCount; }
    public void setPendingPickupsCount(long pendingPickupsCount) { this.pendingPickupsCount = pendingPickupsCount; }

    public long getCompletedPickupsCount() { return completedPickupsCount; }
    public void setCompletedPickupsCount(long completedPickupsCount) { this.completedPickupsCount = completedPickupsCount; }

    public double getTotalQuantityReceivedKg() { return totalQuantityReceivedKg; }
    public void setTotalQuantityReceivedKg(double totalQuantityReceivedKg) { this.totalQuantityReceivedKg = totalQuantityReceivedKg; }

    public double getEstimatedMealsDistributed() { return estimatedMealsDistributed; }
    public void setEstimatedMealsDistributed(double estimatedMealsDistributed) { this.estimatedMealsDistributed = estimatedMealsDistributed; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public List<DonationResponse> getRecommendedDonations() { return recommendedDonations; }
    public void setRecommendedDonations(List<DonationResponse> recommendedDonations) { this.recommendedDonations = recommendedDonations; }

    public List<ClaimResponse> getActiveClaims() { return activeClaims; }
    public void setActiveClaims(List<ClaimResponse> activeClaims) { this.activeClaims = activeClaims; }

    public List<NeedResponse> getCurrentNeeds() { return currentNeeds; }
    public void setCurrentNeeds(List<NeedResponse> currentNeeds) { this.currentNeeds = currentNeeds; }
}
