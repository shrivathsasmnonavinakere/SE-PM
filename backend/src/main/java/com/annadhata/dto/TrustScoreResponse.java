package com.annadhata.dto;

import com.annadhata.enums.Role;
import com.annadhata.model.TrustScore;

import java.time.Instant;
import java.util.List;

public class TrustScoreResponse {
    private String userId;
    private String userName;
    private Role role;
    private double overallScore;
    private String tier;
    private List<TrustScore.TrustFactor> factors;
    private int completedCount;
    private int totalTransactions;
    private int onTimePickups;
    private int missedPickups;
    private int cancelledCount;
    private int safetyFlagsCount;
    private String explanation;
    private Instant calculatedAt;

    public TrustScoreResponse() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public List<TrustScore.TrustFactor> getFactors() { return factors; }
    public void setFactors(List<TrustScore.TrustFactor> factors) { this.factors = factors; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public int getOnTimePickups() { return onTimePickups; }
    public void setOnTimePickups(int onTimePickups) { this.onTimePickups = onTimePickups; }

    public int getMissedPickups() { return missedPickups; }
    public void setMissedPickups(int missedPickups) { this.missedPickups = missedPickups; }

    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

    public int getSafetyFlagsCount() { return safetyFlagsCount; }
    public void setSafetyFlagsCount(int safetyFlagsCount) { this.safetyFlagsCount = safetyFlagsCount; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}

