package com.annadhata;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiExtractionResponse;
import com.annadhata.enums.FoodCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeminiAIServiceTest {

    private GeminiAIService geminiAIService;

    @BeforeEach
    void setUp() {
        geminiAIService = new GeminiAIService();
    }

    @Test
    @DisplayName("AI extraction extracts quantity, unit, food name and category from natural language prompt")
    void testNaturalLanguageExtraction() {
        String prompt = "I have 25 kg of vegetable biryani prepared at 7 PM. It must be collected before 9 PM.";
        AiExtractionResponse response = geminiAIService.extractDonationDetails(prompt);

        assertNotNull(response);
        assertEquals(25.0, response.getQuantity());
        assertEquals("kg", response.getUnit());
        assertEquals(FoodCategory.COOKED_FOOD, response.getFoodCategory());
        assertTrue(response.getFoodName().toLowerCase().contains("biryani"));
        assertNotNull(response.getPreparationDateTime());
        assertNotNull(response.getPickupDeadline());
    }

    @Test
    @DisplayName("AI extraction parses bakery and bread items correctly")
    void testBakeryExtraction() {
        String prompt = "Surplus 50 packets of fresh bread and buns available from evening bake.";
        AiExtractionResponse response = geminiAIService.extractDonationDetails(prompt);

        assertNotNull(response);
        assertEquals(50.0, response.getQuantity());
        assertEquals("packets", response.getUnit());
        assertEquals(FoodCategory.BREAD_AND_BAKERY, response.getFoodCategory());
    }
}
