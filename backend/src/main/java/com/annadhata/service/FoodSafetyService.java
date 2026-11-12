package com.annadhata.service;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiSafetyEvaluationResponse;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.model.Donation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class FoodSafetyService {

    private final GeminiAIService geminiAIService;

    public FoodSafetyService(GeminiAIService geminiAIService) {
        this.geminiAIService = geminiAIService;
    }

    public AiSafetyEvaluationResponse screenDonation(Donation donation) {
        // Run safety analysis through GeminiAIService which conducts both deterministic & AI checks
        AiSafetyEvaluationResponse response = geminiAIService.evaluateFoodSafety(donation);

        // Update donation entity safety indicator
        donation.setSafetyIndicator(response.getSafetyIndicator());
        if (response.getIdentifiedConcerns() != null && !response.getIdentifiedConcerns().isEmpty()) {
            donation.setSafetyNotes(String.join("; ", response.getIdentifiedConcerns()));
        } else {
            donation.setSafetyNotes("Standard safety checks passed.");
        }

        return response;
    }

    public boolean isExpired(Donation donation) {
        if (donation.getPickupDeadline() == null) return false;
        return donation.getPickupDeadline().isBefore(Instant.now());
    }

    public long getHoursUntilExpiry(Donation donation) {
        if (donation.getPickupDeadline() == null) return 0;
        return Duration.between(Instant.now(), donation.getPickupDeadline()).toHours();
    }
}
