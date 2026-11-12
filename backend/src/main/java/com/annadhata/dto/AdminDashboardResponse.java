package com.annadhata.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {
    private long totalUsers;
    private long totalDonors;
    private long totalReceivers;
    private long totalVolunteers;
    private long verifiedOrganisations;
    private long pendingVerificationRequests;
    private long activeRescueRelaysCount;
    private long totalDonations;
    private long availableDonations;
    private long completedDonations;
    private long expiredDonations;
    private double totalFoodRedistributedKg;
    private double estimatedMealsSaved;
    private double estimatedCo2AvoidedKg;
    private double redistributionSuccessRate;
    private Map<String, Long> foodCategoryDistribution;
    private List<DonationResponse> flaggedSafetyDonations = new ArrayList<>();
    private List<UserProfileResponse> pendingVerifications = new ArrayList<>();

    public AdminDashboardResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalDonors() { return totalDonors; }
    public void setTotalDonors(long totalDonors) { this.totalDonors = totalDonors; }

    public long getTotalReceivers() { return totalReceivers; }
    public void setTotalReceivers(long totalReceivers) { this.totalReceivers = totalReceivers; }

    public long getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(long totalVolunteers) { this.totalVolunteers = totalVolunteers; }

    public long getActiveRescueRelaysCount() { return activeRescueRelaysCount; }
    public void setActiveRescueRelaysCount(long activeRescueRelaysCount) { this.activeRescueRelaysCount = activeRescueRelaysCount; }

    public long getVerifiedOrganisations() { return verifiedOrganisations; }
    public void setVerifiedOrganisations(long verifiedOrganisations) { this.verifiedOrganisations = verifiedOrganisations; }

    public long getPendingVerificationRequests() { return pendingVerificationRequests; }
    public void setPendingVerificationRequests(long pendingVerificationRequests) { this.pendingVerificationRequests = pendingVerificationRequests; }

    public long getTotalDonations() { return totalDonations; }
    public void setTotalDonations(long totalDonations) { this.totalDonations = totalDonations; }

    public long getAvailableDonations() { return availableDonations; }
    public void setAvailableDonations(long availableDonations) { this.availableDonations = availableDonations; }

    public long getCompletedDonations() { return completedDonations; }
    public void setCompletedDonations(long completedDonations) { this.completedDonations = completedDonations; }

    public long getExpiredDonations() { return expiredDonations; }
    public void setExpiredDonations(long expiredDonations) { this.expiredDonations = expiredDonations; }

    public double getTotalFoodRedistributedKg() { return totalFoodRedistributedKg; }
    public void setTotalFoodRedistributedKg(double totalFoodRedistributedKg) { this.totalFoodRedistributedKg = totalFoodRedistributedKg; }

    public double getEstimatedMealsSaved() { return estimatedMealsSaved; }
    public void setEstimatedMealsSaved(double estimatedMealsSaved) { this.estimatedMealsSaved = estimatedMealsSaved; }

    public double getEstimatedCo2AvoidedKg() { return estimatedCo2AvoidedKg; }
    public void setEstimatedCo2AvoidedKg(double estimatedCo2AvoidedKg) { this.estimatedCo2AvoidedKg = estimatedCo2AvoidedKg; }

    public double getRedistributionSuccessRate() { return redistributionSuccessRate; }
    public void setRedistributionSuccessRate(double redistributionSuccessRate) { this.redistributionSuccessRate = redistributionSuccessRate; }

    public Map<String, Long> getFoodCategoryDistribution() { return foodCategoryDistribution; }
    public void setFoodCategoryDistribution(Map<String, Long> foodCategoryDistribution) { this.foodCategoryDistribution = foodCategoryDistribution; }

    public List<DonationResponse> getFlaggedSafetyDonations() { return flaggedSafetyDonations; }
    public void setFlaggedSafetyDonations(List<DonationResponse> flaggedSafetyDonations) { this.flaggedSafetyDonations = flaggedSafetyDonations; }

    public List<UserProfileResponse> getPendingVerifications() { return pendingVerifications; }
    public void setPendingVerifications(List<UserProfileResponse> pendingVerifications) { this.pendingVerifications = pendingVerifications; }
}
