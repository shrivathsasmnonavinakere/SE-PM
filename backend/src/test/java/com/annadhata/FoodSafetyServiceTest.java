package com.annadhata;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiSafetyEvaluationResponse;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.model.Donation;
import com.annadhata.service.FoodSafetyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FoodSafetyServiceTest {

    private FoodSafetyService foodSafetyService;
    private GeminiAIService geminiAIService;

    @BeforeEach
    void setUp() {
        geminiAIService = new GeminiAIService();
        foodSafetyService = new FoodSafetyService(geminiAIService);
    }

    @Test
    @DisplayName("Donation past deadline is flagged as EXPIRED")
    void testExpiredDonationFlagged() {
        Donation donation = new Donation();
        donation.setFoodName("Leftover Meals");
        donation.setFoodCategory(FoodCategory.COOKED_FOOD);
        donation.setPreparationDateTime(Instant.now().minus(6, ChronoUnit.HOURS));
        donation.setPickupDeadline(Instant.now().minus(1, ChronoUnit.HOURS)); // 1 hr in past

        AiSafetyEvaluationResponse response = foodSafetyService.screenDonation(donation);

        assertEquals(SafetyIndicator.EXPIRED, response.getSafetyIndicator());
        assertTrue(response.isRequiresAdminReview());
        assertTrue(foodSafetyService.isExpired(donation));
    }

    @Test
    @DisplayName("Fresh donation within normal parameters receives LOW_RISK or standard review")
    void testFreshDonationSafety() {
        Donation donation = new Donation();
        donation.setFoodName("20 kg Fresh Biryani");
        donation.setFoodCategory(FoodCategory.COOKED_FOOD);
        donation.setPreparationDateTime(Instant.now().minus(1, ChronoUnit.HOURS));
        donation.setPickupDeadline(Instant.now().plus(3, ChronoUnit.HOURS));
        donation.setFoodHandlingDetails("Hot holding at 68C");
        donation.setStorageInformation("Stainless steel food containers");

        AiSafetyEvaluationResponse response = foodSafetyService.screenDonation(donation);

        assertNotEquals(SafetyIndicator.EXPIRED, response.getSafetyIndicator());
        assertFalse(foodSafetyService.isExpired(donation));
    }
}
