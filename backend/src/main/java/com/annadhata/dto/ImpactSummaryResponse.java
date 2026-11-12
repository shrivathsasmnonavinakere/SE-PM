package com.annadhata.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImpactSummaryResponse {
    private double totalFoodDonatedKg;
    private double totalFoodRedistributedKg;
    private long totalCompletedDonations;
    private double estimatedMealsSaved;
    private double estimatedCo2AvoidedKg;
    private double successRatePercent;
    private long activeDonorsCount;
    private long activeReceiversCount;
    private Map<String, Double> categoryRedistributedKg;
    private List<MonthlyImpactStat> monthlyTrends = new ArrayList<>();
    private String mealEstimationFactor = "1 meal = 0.4 kg (2.5 meals per kg food)";
    private String co2EstimationFactor = "2.5 kg CO2e avoided per 1 kg food waste diverted from landfill (UNEP/FAO methodology)";
    private String disclaimer = "Impact figures reflect estimated meals and greenhouse gas reductions modeled on empirical food conversion metrics.";

    public static class MonthlyImpactStat {
        private String month;
        private double foodKg;
        private double mealsSaved;

        public MonthlyImpactStat() {}

        public MonthlyImpactStat(String month, double foodKg, double mealsSaved) {
            this.month = month;
            this.foodKg = foodKg;
            this.mealsSaved = mealsSaved;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getFoodKg() { return foodKg; }
        public void setFoodKg(double foodKg) { this.foodKg = foodKg; }

        public double getMealsSaved() { return mealsSaved; }
        public void setMealsSaved(double mealsSaved) { this.mealsSaved = mealsSaved; }
    }

    public ImpactSummaryResponse() {}

    public double getTotalFoodDonatedKg() { return totalFoodDonatedKg; }
    public void setTotalFoodDonatedKg(double totalFoodDonatedKg) { this.totalFoodDonatedKg = totalFoodDonatedKg; }

    public double getTotalFoodRedistributedKg() { return totalFoodRedistributedKg; }
    public void setTotalFoodRedistributedKg(double totalFoodRedistributedKg) { this.totalFoodRedistributedKg = totalFoodRedistributedKg; }

    public long getTotalCompletedDonations() { return totalCompletedDonations; }
    public void setTotalCompletedDonations(long totalCompletedDonations) { this.totalCompletedDonations = totalCompletedDonations; }

    public double getEstimatedMealsSaved() { return estimatedMealsSaved; }
    public void setEstimatedMealsSaved(double estimatedMealsSaved) { this.estimatedMealsSaved = estimatedMealsSaved; }

    public double getEstimatedCo2AvoidedKg() { return estimatedCo2AvoidedKg; }
    public void setEstimatedCo2AvoidedKg(double estimatedCo2AvoidedKg) { this.estimatedCo2AvoidedKg = estimatedCo2AvoidedKg; }

    public double getSuccessRatePercent() { return successRatePercent; }
    public void setSuccessRatePercent(double successRatePercent) { this.successRatePercent = successRatePercent; }

    public long getActiveDonorsCount() { return activeDonorsCount; }
    public void setActiveDonorsCount(long activeDonorsCount) { this.activeDonorsCount = activeDonorsCount; }

    public long getActiveReceiversCount() { return activeReceiversCount; }
    public void setActiveReceiversCount(long activeReceiversCount) { this.activeReceiversCount = activeReceiversCount; }

    public Map<String, Double> getCategoryRedistributedKg() { return categoryRedistributedKg; }
    public void setCategoryRedistributedKg(Map<String, Double> categoryRedistributedKg) { this.categoryRedistributedKg = categoryRedistributedKg; }

    public List<MonthlyImpactStat> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyImpactStat> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public String getMealEstimationFactor() { return mealEstimationFactor; }
    public void setMealEstimationFactor(String mealEstimationFactor) { this.mealEstimationFactor = mealEstimationFactor; }

    public String getCo2EstimationFactor() { return co2EstimationFactor; }
    public void setCo2EstimationFactor(String co2EstimationFactor) { this.co2EstimationFactor = co2EstimationFactor; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
