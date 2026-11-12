package com.annadhata.dto;

import com.annadhata.enums.SafetyIndicator;

import java.util.ArrayList;
import java.util.List;

public class AiSafetyEvaluationResponse {
    private SafetyIndicator safetyIndicator;
    private String summary;
    private List<String> identifiedConcerns = new ArrayList<>();
    private List<String> recommendedActions = new ArrayList<>();
    private boolean requiresAdminReview;
    private String disclaimer = "Preliminary software safety screening indicator only. Not a biological or laboratory food safety certification.";

    public AiSafetyEvaluationResponse() {}

    public SafetyIndicator getSafetyIndicator() { return safetyIndicator; }
    public void setSafetyIndicator(SafetyIndicator safetyIndicator) { this.safetyIndicator = safetyIndicator; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getIdentifiedConcerns() { return identifiedConcerns; }
    public void setIdentifiedConcerns(List<String> identifiedConcerns) { this.identifiedConcerns = identifiedConcerns; }

    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }

    public boolean isRequiresAdminReview() { return requiresAdminReview; }
    public void setRequiresAdminReview(boolean requiresAdminReview) { this.requiresAdminReview = requiresAdminReview; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
