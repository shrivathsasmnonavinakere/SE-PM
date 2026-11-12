package com.annadhata.controller;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.analytics.AnalyticsService;
import com.annadhata.dto.AiAnalyticsReportResponse;
import com.annadhata.dto.AiChatRequest;
import com.annadhata.dto.AiChatResponse;
import com.annadhata.dto.AiExtractionRequest;
import com.annadhata.dto.AiExtractionResponse;
import com.annadhata.dto.AiSafetyEvaluationResponse;
import com.annadhata.dto.AiSafetyScreenRequest;
import com.annadhata.dto.ApiResponse;
import com.annadhata.model.ChatHistory;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.service.ChatbotService;
import com.annadhata.service.FoodSafetyService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "Google Gemini AI Services", description = "Endpoints for natural language donation extraction, safety screening, AI chatbot, and analytics")
public class AiController {

    private final GeminiAIService geminiAIService;
    private final FoodSafetyService foodSafetyService;
    private final AnalyticsService analyticsService;
    private final ChatbotService chatbotService;

    public AiController(GeminiAIService geminiAIService,
                        FoodSafetyService foodSafetyService,
                        AnalyticsService analyticsService,
                        ChatbotService chatbotService) {
        this.geminiAIService = geminiAIService;
        this.foodSafetyService = foodSafetyService;
        this.analyticsService = analyticsService;
        this.chatbotService = chatbotService;
    }

    @PostMapping("/extract-donation")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Extract structured donation details from natural language text using Gemini AI")
    public ResponseEntity<ApiResponse<AiExtractionResponse>> extractDonation(@Valid @RequestBody AiExtractionRequest request) {
        AiExtractionResponse response = geminiAIService.extractDonationDetails(request.getText());
        return ResponseEntity.ok(ApiResponse.success("Donation details extracted successfully", response));
    }

    @PostMapping("/screen-safety")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Perform software food safety screening on prospective donation parameters")
    public ResponseEntity<ApiResponse<AiSafetyEvaluationResponse>> screenSafety(@Valid @RequestBody AiSafetyScreenRequest request) {
        Donation transientDonation = new Donation();
        transientDonation.setFoodName(request.getFoodName());
        transientDonation.setFoodDescription(request.getFoodDescription());
        transientDonation.setFoodCategory(request.getFoodCategory());
        transientDonation.setPreparationDateTime(request.getPreparationDateTime());
        transientDonation.setPickupDeadline(request.getPickupDeadline());
        transientDonation.setFoodHandlingDetails(request.getFoodHandlingDetails());
        transientDonation.setStorageInformation(request.getStorageInformation());
        AiSafetyEvaluationResponse response = foodSafetyService.screenDonation(transientDonation);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics-report")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Generate AI strategic insights report over real aggregated platform records")
    public ResponseEntity<ApiResponse<AiAnalyticsReportResponse>> getAiAnalyticsReport() {
        AiAnalyticsReportResponse report = analyticsService.generateAggregatedReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping("/public/chat")
    @Operation(summary = "Public FAQ chat endpoint for unauthenticated visitors")
    public ResponseEntity<ApiResponse<AiChatResponse>> publicChat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = chatbotService.handlePublicMessage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with AnnaDhata AI Assistant (authenticated, role-aware)")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        User user = SecurityUtils.getCurrentUser();
        AiChatResponse response = chatbotService.handleUserMessage(request, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/chat-history")
    @Operation(summary = "Get previous chat history with AI Assistant")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getChatHistory() {
        User user = SecurityUtils.getCurrentUser();
        List<ChatHistory> history = chatbotService.getConversationHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
