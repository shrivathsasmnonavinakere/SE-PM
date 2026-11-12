package com.annadhata.model;

import com.annadhata.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trustScores")
public class TrustScore {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String userName;
    private Role role;
    private Double overallScore = 80.0;
    private String tier = "GOLD";

    private List<TrustFactor> factors = new ArrayList<>();

    private Integer completedCount = 0;
    private Integer totalTransactions = 0;
    private Integer onTimePickups = 0;
    private Integer missedPickups = 0;
    private Integer cancelledCount = 0;
    private Integer safetyFlagsCount = 0;

    private String explanation;
    private Instant calculatedAt = Instant.now();

    public TrustScore() {}

    public static class TrustFactor {
        private String factorName;
        private Integer weightPercent;
        private Double score;
        private String detail;

        public TrustFactor() {}

        public TrustFactor(String factorName, Integer weightPercent, Double score, String detail) {
            this.factorName = factorName;
            this.weightPercent = weightPercent;
            this.score = score;
            this.detail = detail;
        }

        public String getFactorName() { return factorName; }
        public void setFactorName(String factorName) { this.factorName = factorName; }

        public Integer getWeightPercent() { return weightPercent; }
        public void setWeightPercent(Integer weightPercent) { this.weightPercent = weightPercent; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public List<TrustFactor> getFactors() { return factors; }
    public void setFactors(List<TrustFactor> factors) { this.factors = factors; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public Integer getOnTimePickups() { return onTimePickups; }
    public void setOnTimePickups(Integer onTimePickups) { this.onTimePickups = onTimePickups; }

    public Integer getMissedPickups() { return missedPickups; }
    public void setMissedPickups(Integer missedPickups) { this.missedPickups = missedPickups; }

    public Integer getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(Integer cancelledCount) { this.cancelledCount = cancelledCount; }

    public Integer getSafetyFlagsCount() { return safetyFlagsCount; }
    public void setSafetyFlagsCount(Integer safetyFlagsCount) { this.safetyFlagsCount = safetyFlagsCount; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}

