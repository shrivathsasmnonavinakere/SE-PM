package com.annadhata.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DonorDashboardResponse {
    private long totalDonations;
    private long activeDonations;
    private long completedDonations;
    private long pendingClaims;
    private double totalFoodDonatedKg;
    private double totalFoodRedistributedKg;
    private double estimatedMealsSaved;
    private double estimatedCo2AvoidedKg;
    private double trustScore;
    private String trustTier;
    private long activeRescueRelaysCount;
    private List<DonationResponse> recentDonations = new ArrayList<>();
    private List<ClaimResponse> recentPendingClaims = new ArrayList<>();
    private Map<String, Long> donationsByCategory;

    public DonorDashboardResponse() {}

    public long getTotalDonations() { return totalDonations; }
    public void setTotalDonations(long totalDonations) { this.totalDonations = totalDonations; }

    public long getActiveDonations() { return activeDonations; }
    public void setActiveDonations(long activeDonations) { this.activeDonations = activeDonations; }

    public long getCompletedDonations() { return completedDonations; }
    public void setCompletedDonations(long completedDonations) { this.completedDonations = completedDonations; }

    public long getPendingClaims() { return pendingClaims; }
    public void setPendingClaims(long pendingClaims) { this.pendingClaims = pendingClaims; }

    public double getTotalFoodDonatedKg() { return totalFoodDonatedKg; }
    public void setTotalFoodDonatedKg(double totalFoodDonatedKg) { this.totalFoodDonatedKg = totalFoodDonatedKg; }

    public double getTotalFoodRedistributedKg() { return totalFoodRedistributedKg; }
    public void setTotalFoodRedistributedKg(double totalFoodRedistributedKg) { this.totalFoodRedistributedKg = totalFoodRedistributedKg; }

    public double getEstimatedMealsSaved() { return estimatedMealsSaved; }
    public void setEstimatedMealsSaved(double estimatedMealsSaved) { this.estimatedMealsSaved = estimatedMealsSaved; }

    public double getEstimatedCo2AvoidedKg() { return estimatedCo2AvoidedKg; }
    public void setEstimatedCo2AvoidedKg(double estimatedCo2AvoidedKg) { this.estimatedCo2AvoidedKg = estimatedCo2AvoidedKg; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public long getActiveRescueRelaysCount() { return activeRescueRelaysCount; }
    public void setActiveRescueRelaysCount(long activeRescueRelaysCount) { this.activeRescueRelaysCount = activeRescueRelaysCount; }

    public List<DonationResponse> getRecentDonations() { return recentDonations; }
    public void setRecentDonations(List<DonationResponse> recentDonations) { this.recentDonations = recentDonations; }

    public List<ClaimResponse> getRecentPendingClaims() { return recentPendingClaims; }
    public void setRecentPendingClaims(List<ClaimResponse> recentPendingClaims) { this.recentPendingClaims = recentPendingClaims; }

    public Map<String, Long> getDonationsByCategory() { return donationsByCategory; }
    public void setDonationsByCategory(Map<String, Long> donationsByCategory) { this.donationsByCategory = donationsByCategory; }
}
