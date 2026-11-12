package com.annadhata.service;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiChatRequest;
import com.annadhata.dto.AiChatResponse;
import com.annadhata.model.ChatHistory;
import com.annadhata.model.User;
import com.annadhata.repository.ChatHistoryRepository;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private final GeminiAIService geminiAIService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final DonationRepository donationRepository;
    private final ClaimRepository claimRepository;

    public ChatbotService(GeminiAIService geminiAIService,
                          ChatHistoryRepository chatHistoryRepository,
                          DonationRepository donationRepository,
                          ClaimRepository claimRepository) {
        this.geminiAIService = geminiAIService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.donationRepository = donationRepository;
        this.claimRepository = claimRepository;
    }

    public AiChatResponse handleUserMessage(AiChatRequest req, User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("role", user.getRole().name());
        context.put("organizationName", user.getOrganizationName());
        context.put("address", user.getAddress());

        if (user.getRole() == com.annadhata.enums.Role.ROLE_DONOR) {
            context.put("totalDonations", donationRepository.countByDonorId(user.getId()));
        } else if (user.getRole() == com.annadhata.enums.Role.ROLE_RECEIVER) {
            context.put("totalClaims", claimRepository.countByReceiverId(user.getId()));
        }

        AiChatResponse response = geminiAIService.generateChatbotReply(req.getMessage(), user, context);

        // Save conversation history in MongoDB
        chatHistoryRepository.save(new ChatHistory(user.getId(), user.getRole(), req.getMessage(), response.getReply()));

        return response;
    }

    public AiChatResponse handlePublicMessage(AiChatRequest req) {
        Map<String, Object> context = new HashMap<>();
        context.put("role", "GUEST");
        return geminiAIService.generateChatbotReply(req.getMessage(), null, context);
    }

    public List<ChatHistory> getConversationHistory(String userId) {
        return chatHistoryRepository.findByUserIdOrderByTimestampAsc(userId);
    }
}
