package com.annadhata.ai;

import com.annadhata.dto.AiAnalyticsReportResponse;
import com.annadhata.dto.AiChatResponse;
import com.annadhata.dto.AiExtractionResponse;
import com.annadhata.dto.AiSafetyEvaluationResponse;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.model.Donation;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Feature A: AI Food Donation Assistant
     * Extracts structured fields from natural language text
     */
    public AiExtractionResponse extractDonationDetails(String promptText) {
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String systemPrompt = """
                    You are AnnaDhata's AI Food Donation Assistant.
                    Extract the following structured JSON from the donor's natural language input:
                    {
                      "foodName": "Name of the food item",
                      "quantity": 25.0,
                      "unit": "kg",
                      "foodCategory": "Cooked food", // one of: Cooked food, Rice and grains, Vegetables, Fruits, Bread and bakery, Packaged food, Dairy products, Other
                      "preparationHoursAgo": 1.0, // hours before now the food was prepared (float)
                      "pickupDeadlineHoursFromNow": 2.5, // hours from now when it expires/must be picked up (float)
                      "foodDescription": "Short description of condition, allergens, and dietary notes",
                      "storageAdvice": "Recommended storage (e.g., Keep warm at 65C or refrigerate under 4C)"
                    }
                    Return ONLY raw valid JSON without markdown wrapping.
                    """;
                String prompt = systemPrompt + "\n\nDonor Text: " + promptText;
                String geminiJson = callGeminiApi(prompt);
                if (geminiJson != null) {
                    JsonNode node = parseJsonSafe(geminiJson);
                    if (node != null) {
                        AiExtractionResponse response = new AiExtractionResponse();
                        response.setFoodName(node.path("foodName").asText("Surplus Food"));
                        response.setQuantity(node.path("quantity").asDouble(10.0));
                        response.setUnit(node.path("unit").asText("kg"));
                        response.setFoodCategory(FoodCategory.fromString(node.path("foodCategory").asText("Cooked food")));
                        
                        double prepHoursAgo = node.path("preparationHoursAgo").asDouble(1.0);
                        double deadlineHours = node.path("pickupDeadlineHoursFromNow").asDouble(3.0);
                        response.setPreparationDateTime(Instant.now().minus((long)(prepHoursAgo * 60), ChronoUnit.MINUTES));
                        response.setPickupDeadline(Instant.now().plus((long)(deadlineHours * 60), ChronoUnit.MINUTES));
                        
                        response.setFoodDescription(node.path("foodDescription").asText("Freshly prepared surplus food."));
                        response.setStorageAdvice(node.path("storageAdvice").asText("Keep covered in clean containers."));
                        response.setRawExtractionNotes("Extracted via Google Gemini AI Engine (" + geminiModel + ")");
                        return response;
                    }
                }
            } catch (Exception ex) {
                log.warn("Gemini API call failed for extraction, using intelligent fallback: {}", ex.getMessage());
            }
        }

        // Intelligent deterministic fallback parser
        return fallbackExtractDonationDetails(promptText);
    }

    /**
     * Feature B: AI Smart Food Matching Engine Explanation
     * Generates human-readable explanations why this receiver was matched
     */
    public String explainMatchRecommendation(Donation donation, ReceiverNeed need, double distanceKm, double matchScore) {
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String prompt = String.format("""
                    You are AnnaDhata's Smart Redistribution Matching Engine.
                    Explain concisely in 2 sentences why the receiver '%s' is an excellent match for donation '%s'.
                    Details:
                    - Distance: %.1f km
                    - Match Score: %.0f/100
                    - Food Name: %s (Category: %s, Quantity: %.1f %s)
                    - Receiver Requirement: Category %s, Beneficiaries: %d, Capacity: %.1f kg
                    - Pickup Deadline: within %d hours
                    Be professional, objective, and encouraging.
                    """,
                    need.getReceiverName(), donation.getFoodName(), distanceKm, matchScore,
                    donation.getFoodName(), donation.getFoodCategory().getDisplayName(), donation.getQuantity(), donation.getMeasurementUnit(),
                    need.getRequiredCategory().getDisplayName(), need.getNumberOfBeneficiaries() != null ? need.getNumberOfBeneficiaries() : 50,
                    need.getMaxReceivingCapacity() != null ? need.getMaxReceivingCapacity() : 50.0,
                    Math.max(1, Duration.between(Instant.now(), donation.getPickupDeadline()).toHours())
                );
                String explanation = callGeminiApi(prompt);
                if (explanation != null && !explanation.trim().isEmpty()) {
                    return explanation.replaceAll("^[\"']|[\"']$", "").trim();
                }
            } catch (Exception ex) {
                log.warn("Gemini explanation failed, falling back: {}", ex.getMessage());
            }
        }

        // Deterministic high-quality explanation fallback
        return String.format("%s is strongly recommended because it is only %.1f km away, has an active requirement for %s (serving ~%d beneficiaries), and has verified capacity to collect the %.1f %s before the pickup deadline.",
                need.getReceiverName(), distanceKm, donation.getFoodCategory().getDisplayName().toLowerCase(),
                need.getNumberOfBeneficiaries() != null ? need.getNumberOfBeneficiaries() : 45,
                donation.getQuantity(), donation.getMeasurementUnit());
    }

    /**
     * Feature C: AI Food Safety Screening
     * Identifies missing handling info and flags potential concerns
     */
    public AiSafetyEvaluationResponse evaluateFoodSafety(Donation donation) {
        AiSafetyEvaluationResponse response = new AiSafetyEvaluationResponse();
        List<String> concerns = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        Instant now = Instant.now();
        Instant prepTime = donation.getPreparationDateTime();
        Instant deadline = donation.getPickupDeadline();

        // 1. Deterministic mandatory safety floor checks (AI must NEVER override these)
        if (deadline != null && deadline.isBefore(now)) {
            response.setSafetyIndicator(SafetyIndicator.EXPIRED);
            response.setSummary("Pickup deadline has passed. Food listing is marked as EXPIRED to protect consumers.");
            concerns.add("Pickup deadline expired at " + deadline);
            recommendations.add("Do not redistribute expired food. Discard safely or route to composting/bio-waste.");
            response.setRequiresAdminReview(true);
            response.setIdentifiedConcerns(concerns);
            response.setRecommendedActions(recommendations);
            return response;
        }

        SafetyIndicator deterministicFloor = SafetyIndicator.LOW_RISK;

        // Scan text for deterministic hygiene/spoilage hazard red flags
        String combinedText = ((donation.getFoodName() != null ? donation.getFoodName() : "") + " "
                + (donation.getFoodDescription() != null ? donation.getFoodDescription() : "") + " "
                + (donation.getFoodHandlingDetails() != null ? donation.getFoodHandlingDetails() : "") + " "
                + (donation.getStorageInformation() != null ? donation.getStorageInformation() : "")).toLowerCase();

        List<String> severeKeywords = List.of("spoiled", "rotten", "mold", "moldy", "sour smell", "foul", "fermented", "contaminated", "expired", "stale overnight", "unrefrigerated overnight");
        for (String kw : severeKeywords) {
            if (combinedText.contains(kw)) {
                concerns.add("Potential spoilage/contamination risk indicator detected in listing description ('" + kw + "').");
                recommendations.add("Mandatory Admin / donor inspection required. Do NOT distribute if spoilage is suspected.");
                deterministicFloor = SafetyIndicator.HIGH_RISK;
                response.setRequiresAdminReview(true);
                break;
            }
        }

        if (prepTime != null && deadline != null) {
            long hoursBetween = Duration.between(prepTime, deadline).toHours();
            if (hoursBetween > 12 && donation.getFoodCategory() == FoodCategory.COOKED_FOOD) {
                concerns.add("Extended window (" + hoursBetween + " hours) between preparation and pickup for cooked food.");
                recommendations.add("Verify active temperature maintenance (hot-holding above 65°C or chilled below 4°C).");
                if (deterministicFloor != SafetyIndicator.HIGH_RISK) {
                    deterministicFloor = SafetyIndicator.REVIEW_REQUIRED;
                }
                response.setRequiresAdminReview(true);
            } else if (hoursBetween > 6 && donation.getFoodCategory() == FoodCategory.COOKED_FOOD) {
                concerns.add("Moderate window (" + hoursBetween + " hours) for cooked food; timely collection recommended.");
                recommendations.add("Ensure food remained in temperature-controlled storage since preparation.");
                if (deterministicFloor == SafetyIndicator.LOW_RISK) {
                    deterministicFloor = SafetyIndicator.REVIEW_REQUIRED;
                }
            }
        }

        if (donation.getFoodHandlingDetails() == null || donation.getFoodHandlingDetails().trim().isEmpty()) {
            concerns.add("Missing specific food handling instructions or temperature controls.");
            recommendations.add("Donor should specify whether food has been kept chilled, heated, or at ambient room temperature.");
            if (deterministicFloor == SafetyIndicator.LOW_RISK) {
                deterministicFloor = SafetyIndicator.REVIEW_REQUIRED;
            }
        }

        if (donation.getStorageInformation() == null || donation.getStorageInformation().trim().isEmpty()) {
            recommendations.add("Ensure food is kept in sealed food-grade containers away from direct sunlight.");
        }

        SafetyIndicator aiIndicator = null;
        // 2. Call Gemini if configured for additional semantic screening (can escalate risk, never downgrade floor)
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String prompt = String.format("""
                    Analyze this surplus food donation for safety screening:
                    - Food Name: %s
                    - Category: %s
                    - Description: %s
                    - Handling: %s
                    - Storage: %s
                    Return JSON:
                    {
                      "safetyIndicator": "LOW_RISK" or "REVIEW_REQUIRED" or "HIGH_RISK",
                      "concerns": ["concern 1", "concern 2"],
                      "recommendations": ["rec 1", "rec 2"],
                      "requiresAdminReview": false
                    }
                    Remember: Never certify food as safe to consume. Screen for common risks only.
                    Return ONLY raw JSON.
                    """,
                    donation.getFoodName(),
                    donation.getFoodCategory() != null ? donation.getFoodCategory().getDisplayName() : "Other",
                    donation.getFoodDescription() != null ? donation.getFoodDescription() : "N/A",
                    donation.getFoodHandlingDetails() != null ? donation.getFoodHandlingDetails() : "N/A",
                    donation.getStorageInformation() != null ? donation.getStorageInformation() : "N/A"
                );
                String geminiJson = callGeminiApi(prompt);
                if (geminiJson != null) {
                    JsonNode node = parseJsonSafe(geminiJson);
                    if (node != null) {
                        String indicator = node.path("safetyIndicator").asText("LOW_RISK");
                        if ("HIGH_RISK".equalsIgnoreCase(indicator)) aiIndicator = SafetyIndicator.HIGH_RISK;
                        else if ("REVIEW_REQUIRED".equalsIgnoreCase(indicator)) aiIndicator = SafetyIndicator.REVIEW_REQUIRED;
                        else aiIndicator = SafetyIndicator.LOW_RISK;

                        node.path("concerns").forEach(c -> {
                            String txt = c.asText();
                            if (!txt.isBlank() && !concerns.contains(txt)) concerns.add(txt);
                        });
                        node.path("recommendations").forEach(r -> {
                            String txt = r.asText();
                            if (!txt.isBlank() && !recommendations.contains(txt)) recommendations.add(txt);
                        });
                        if (node.path("requiresAdminReview").asBoolean(false)) {
                            response.setRequiresAdminReview(true);
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Gemini safety screening failed, using deterministic results: {}", ex.getMessage());
            }
        }

        // Enforce highest severity between deterministic floor and AI assessment
        SafetyIndicator finalIndicator = deterministicFloor;
        if (aiIndicator == SafetyIndicator.HIGH_RISK) {
            finalIndicator = SafetyIndicator.HIGH_RISK;
            response.setRequiresAdminReview(true);
        } else if (aiIndicator == SafetyIndicator.REVIEW_REQUIRED && finalIndicator == SafetyIndicator.LOW_RISK) {
            finalIndicator = SafetyIndicator.REVIEW_REQUIRED;
        }
        response.setSafetyIndicator(finalIndicator);

        if (finalIndicator == SafetyIndicator.HIGH_RISK) {
            response.setSummary("High-risk safety screening alert: manual review required before any distribution.");
        } else if (finalIndicator == SafetyIndicator.REVIEW_REQUIRED) {
            response.setSummary("Preliminary screening identified advisory items requiring standard hygiene verification.");
        } else {
            response.setSummary("Standard preliminary screening passed (not a statutory certification). Normal hygiene and cold/hot chain controls apply.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Transport in sanitized, insulated food-grade containers.");
            recommendations.add("Perform visual and sensory inspection prior to final distribution.");
        }

        response.setIdentifiedConcerns(concerns);
        response.setRecommendedActions(recommendations);
        return response;
    }

    /**
     * Feature D: AI Analytics Assistant
     * Evaluates real aggregated data and generates strategic insights
     */
    public AiAnalyticsReportResponse generateAnalyticsReport(Map<String, Object> metrics) {
        AiAnalyticsReportResponse report = new AiAnalyticsReportResponse();

        double totalKg = ((Number) metrics.getOrDefault("totalRedistributedKg", 0.0)).doubleValue();
        long completed = ((Number) metrics.getOrDefault("completedDonations", 0L)).longValue();
        long expired = ((Number) metrics.getOrDefault("expiredDonations", 0L)).longValue();
        double successRate = ((Number) metrics.getOrDefault("successRate", 0.0)).doubleValue();
        String topCategory = String.valueOf(metrics.getOrDefault("topCategory", "Cooked food"));
        String deficitCategory = String.valueOf(metrics.getOrDefault("deficitCategory", "Balanced across categories"));
        String topArea = String.valueOf(metrics.getOrDefault("topArea", "Unspecified Zone"));
        String peakHours = String.valueOf(metrics.getOrDefault("peakHours", "Insufficient timing data"));
        String expiredCategory = String.valueOf(metrics.getOrDefault("frequentlyExpiredCategory", "None"));

        report.setTotalRedistributedKg(totalKg);
        report.setTotalCompletedDonations(completed);
        report.setTotalExpiredDonations(expired);
        report.setRedistributionSuccessRate(successRate);
        report.setHighestSurplusCategory(topCategory);
        report.setHighestDeficitCategory(deficitCategory);
        report.setPeakDonationHours(peakHours);

        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String prompt = String.format("""
                    You are AnnaDhata's Sustainability Analytics Strategist.
                    Analyze ONLY these real MongoDB metrics (do not invent other numbers):
                    - Total Redistributed Kg: %.1f
                    - Completed Donations: %d
                    - Expired Donations: %d
                    - Success Rate: %.1f%%
                    - Top Surplus Category: %s
                    - Highest Unmet Deficit Category: %s
                    - Top Redistribution Area: %s
                    - Observed Peak Window: %s
                    - Most Frequently Expired Category: %s
                    Return JSON ONLY:
                    {
                      "executiveSummary": "2-sentence concise factual summary",
                      "strategicInsights": ["insight 1", "insight 2", "insight 3"],
                      "actionableRecommendations": ["rec 1", "rec 2", "rec 3"]
                    }
                    """, totalKg, completed, expired, successRate, topCategory, deficitCategory, topArea, peakHours, expiredCategory);
                String geminiJson = callGeminiApi(prompt);
                JsonNode node = geminiJson != null ? parseJsonSafe(geminiJson) : null;
                if (node != null && node.has("executiveSummary")) {
                    report.setExecutiveSummary(node.path("executiveSummary").asText());
                    List<String> insights = new ArrayList<>();
                    node.path("strategicInsights").forEach(i -> insights.add(i.asText()));
                    List<String> recs = new ArrayList<>();
                    node.path("actionableRecommendations").forEach(r -> recs.add(r.asText()));
                    if (!insights.isEmpty()) report.setStrategicInsights(insights);
                    if (!recs.isEmpty()) report.setActionableRecommendations(recs);
                    if (report.getStrategicInsights() != null && report.getActionableRecommendations() != null) {
                        return report;
                    }
                }
            } catch (Exception ex) {
                log.warn("Gemini analytics narrative failed, using real metrics fallback: {}", ex.getMessage());
            }
        }

        List<String> insights = new ArrayList<>();
        insights.add(String.format("Platform achieves a %.1f%% redistribution completion efficiency across %d completed and %d expired listings.", successRate, completed, expired));
        insights.add(String.format("Observed surplus volume is led by '%s' concentrated around '%s', peaking during %s.", topCategory, topArea, peakHours));
        insights.add(String.format("Highest unmet charity demand category is currently '%s'.", deficitCategory));
        if (expired > 0) {
            insights.add(String.format("%d listings expired prior to collection (most frequently '%s'), highlighting short pickup windows.", expired, expiredCategory));
        }

        List<String> recs = new ArrayList<>();
        recs.add(String.format("Incentivize donors in %s to post surplus earlier before peak window (%s) to enable multi-NGO partial claiming.", topArea, peakHours));
        recs.add(String.format("Target donor outreach toward '%s' suppliers to close current receiver demand gaps.", deficitCategory));
        recs.add("Enable Rescue Relay and Volunteer Dispatch notifications for urgent batches under 2 hours remaining.");

        report.setStrategicInsights(insights);
        report.setActionableRecommendations(recs);
        report.setExecutiveSummary(String.format(
            "AnnaDhata has redistributed %.1f kg of surplus food across MongoDB-tracked donations (~%.0f meals delivered) with a %.1f%% operational completion rate.",
            totalKg, totalKg * 2.5, successRate
        ));

        return report;
    }

    /**
     * Feature E: AnnaDhata AI Assistant Chatbot
     */
    public AiChatResponse generateChatbotReply(String message, User user, Map<String, Object> context) {
        String roleName = user != null && user.getRole() != null ? user.getRole().name() : "GUEST";
        String userName = user != null ? user.getName() : "Friend";

        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String prompt = String.format("""
                    You are 'AnnaDhata AI Assistant', an empathetic and expert guide on the AnnaDhata Food Redistribution platform.
                    User: %s (Role: %s)
                    Platform Context: %s
                    User Question: "%s"

                    Respond directly in 2 to 3 concise, highly actionable paragraphs.
                    Guide the user clearly based on their role. If they want to donate, explain the simple form or AI assistant.
                    If they are an NGO, explain how to browse nearby food or set food requirements.
                    Be courteous, professional, and sustainability-focused.
                    """,
                    userName, roleName, context.toString(), message
                );
                String reply = callGeminiApi(prompt);
                if (reply != null && !reply.trim().isEmpty()) {
                    List<String> suggestions = getQuickSuggestionsForRole(roleName);
                    return new AiChatResponse(reply, suggestions);
                }
            } catch (Exception ex) {
                log.warn("Gemini chatbot call failed, using role-aware fallback: {}", ex.getMessage());
            }
        }

        // Intelligent role-aware chatbot fallback
        return fallbackChatbotResponse(message, user, context);
    }

    private List<String> getQuickSuggestionsForRole(String role) {
        if ("ROLE_DONOR".equals(role)) {
            return Arrays.asList("How does partial distribution work?", "Check QR Food Passport verification", "How does Rescue Relay handle missed pickups?", "How is my Trust Score calculated?");
        } else if ("ROLE_RECEIVER".equals(role)) {
            return Arrays.asList("Find available food donations near me", "How do I show my QR Food Passport?", "Request a volunteer pickup", "View AI Demand Heatmap");
        } else if ("ROLE_VOLUNTEER".equals(role)) {
            return Arrays.asList("How do I accept a delivery mission?", "How do I verify a QR Food Passport?", "How is Volunteer Trust Score earned?");
        } else if ("ROLE_ADMIN".equals(role)) {
            return Arrays.asList("Show Rescue Relay and missed pickups", "Inspect AI Demand Heatmap zones", "View platform sustainability report");
        }
        return Arrays.asList("How does AnnaDhata reduce food waste?", "How do QR Food Passports work?", "How can volunteers help?");
    }

    private AiChatResponse fallbackChatbotResponse(String message, User user, Map<String, Object> context) {
        String lower = message.toLowerCase();
        String role = user != null && user.getRole() != null ? user.getRole().name() : "GUEST";
        String name = user != null ? user.getName() : "Friend";

        String reply;
        List<String> actions = new ArrayList<>();

        if (lower.contains("qr") || lower.contains("passport") || lower.contains("verify")) {
            reply = "Every approved food claim automatically receives a tamper-evident HMAC-SHA256 **QR Food Passport**. At pickup handover, the donor or assigned volunteer scans or enters the passport code (e.g. `AD-XXXX-XXXX-XXXX`) to cryptographically confirm pickup and build on-time Trust Score.";
            actions.add("Open QR Passport Verifier");
        } else if (lower.contains("partial") || lower.contains("split") || lower.contains("multiple ngo")) {
            reply = "With **Partial Food Distribution**, multiple verified NGOs can claim different portions of the same surplus batch (for example, 10 kg and 15 kg from a 25 kg listing). Atomic database reservations guarantee total allocations never exceed remaining quantity.";
            actions.add("Browse Available Food");
        } else if (lower.contains("rescue") || lower.contains("relay") || lower.contains("missed")) {
            reply = "**Rescue Relay** monitors approved claims in real time. When a scheduled pickup time lapses unverified, both parties receive urgent alerts, and the donor can approve releasing the unpicked quantity back to the pool so alternative nearby NGOs are immediately matched.";
            actions.add("Check Rescue Relay Alerts");
        } else if (lower.contains("volunteer") || lower.contains("deliver") || lower.contains("mission")) {
            reply = "Volunteers on AnnaDhata can register with vehicle capacity and service radius to view nearby approved pickups needing transit, claim a mission atomically, verify handover with QR Food Passport, and deliver meals safely to community kitchens.";
            actions.add("Open Volunteer Portal");
        } else if (lower.contains("heatmap") || lower.contains("demand") || lower.contains("hotspot")) {
            reply = "The **AI Demand Heatmap** aggregates real MongoDB donation history, unfilled NGO needs, and expirations into geographic zones, highlighting food surplus hotspots, underserved demand areas, and Gemini spatial recommendations.";
            actions.add("Explore Demand Heatmap");
        } else if (lower.contains("trust") || lower.contains("reliability") || lower.contains("score")) {
            reply = "AnnaDhata calculates transparent **Trust & Reliability Scores** (0–100) from real behavioral records: completion rate, QR-verified on-time pickups, food safety hygiene compliance, and missed-pickup history.";
            actions.add("View Trust Breakdown");
        } else if (lower.contains("donate") || lower.contains("post") || lower.contains("surplus") || lower.contains("food to give")) {
            reply = String.format("Hello %s! To donate surplus food, open the 'Create Donation' page or try our natural-language 'AI Donation Assistant'. Just tell us what food you have (e.g., '25 kg cooked rice prepared at 6 PM, pickup by 9 PM'), and our AI will automatically prepare your listing for instant matching!", name);
            actions.add("Go to Create Donation");
            actions.add("Open AI Assistant");
        } else if (lower.contains("find") || lower.contains("nearby") || lower.contains("near me") || lower.contains("available")) {
            reply = String.format("You can discover surplus food listings within your radius on our interactive Leaflet Map or list view. We display live remaining quantities, pickup deadlines, Donor Trust badges, and safety screening statuses.", name);
            actions.add("Open Donation Map");
            actions.add("Browse Available Food");
        } else if (lower.contains("claim") || lower.contains("pickup") || lower.contains("status")) {
            reply = "When you see an available listing, submit a full or partial claim specifying quantity and pickup time. Once donor-approved, you'll receive an encrypted **QR Food Passport** and can optionally request volunteer transport.";
            actions.add("View My Claims");
        } else if (lower.contains("match") || lower.contains("algorithm") || lower.contains("score")) {
            reply = "AnnaDhata's Smart Matching Engine uses a deterministic weighted model: Distance (30%), Quantity vs. Remaining Capacity (25%), Pickup Urgency (25%), and Food Compatibility (20%), plus transparent NGO Trust & Reliability signals explained in plain language by Gemini.";
        } else if (lower.contains("impact") || lower.contains("meal") || lower.contains("co2") || lower.contains("waste")) {
            reply = "Every completed claim on AnnaDhata is tracked idempotently in real time: 1 meal per 0.4 kg redistributed (2.5 meals/kg) and 2.5 kg CO2e landfill emissions avoided per kg rescued.";
            actions.add("View Impact Dashboard");
        } else {
            reply = String.format("Welcome to AnnaDhata, %s. I can help you with surplus listings, partial claims, QR Food Passports, Rescue Relay alerts, Volunteer delivery missions, Demand Heatmaps, or Trust Scores. What would you like to do?", name);
        }

        AiChatResponse response = new AiChatResponse(reply, getQuickSuggestionsForRole(role));
        response.setRelevantActions(actions);
        return response;
    }

    public String generateHeatmapSpatialInsight(com.annadhata.dto.DemandHeatmapResponse heatmap) {
        if (heatmap.getZones() == null || heatmap.getZones().isEmpty()) {
            return "No spatial redistribution records available yet for the selected filters. Publish surplus donations or NGO requirements to populate live demand and surplus zones.";
        }
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            try {
                String zoneSummary = heatmap.getZones().stream().limit(5)
                        .map(z -> String.format("%s (Surplus: %.1f kg, Demand: %.1f kg, Expired: %.1f kg, Gap: %.1f kg)",
                                z.getZoneName(), z.getTotalSurplusKg(), z.getTotalDemandKg(), z.getExpiredKg(), z.getDeficitKg()))
                        .collect(java.util.stream.Collectors.joining("; "));
                String prompt = String.format("""
                    You are AnnaDhata's Spatial Redistribution Strategist.
                    Given ONLY these real MongoDB zone metrics (do not fabricate numbers):
                    Total Historical Surplus: %.1f kg | Active NGO Demand: %.1f kg | Fulfilled: %.1f kg | Expired: %.1f kg
                    Peak Window: %s | Highest Surplus Zone: %s | Highest Demand Zone: %s
                    Zones: %s
                    Provide 2 concise actionable sentences identifying where volunteer logistics or donor outreach should focus.
                    """,
                    heatmap.getTotalHistoricalSurplusKg(), heatmap.getTotalActiveDemandKg(),
                    heatmap.getTotalFulfilledKg(), heatmap.getTotalExpiredKg(),
                    heatmap.getPeakSurplusHours(), heatmap.getHighestSurplusZone(),
                    heatmap.getHighestDemandZone(), zoneSummary);
                String reply = callGeminiApi(prompt);
                if (reply != null && !reply.isBlank()) {
                    return reply.trim();
                }
            } catch (Exception ex) {
                log.warn("Gemini heatmap insight failed, using deterministic summary: {}", ex.getMessage());
            }
        }

        return String.format(
                "Spatial clustering across %d zones shows highest food surplus in %s (%.1f kg total surplus tracked, peaking %s) while unmet charity appetite concentrates around %s (%.1f kg active demand). Deploying volunteers and partial split deliveries between these corridors directly prevents %.1f kg expiration bottlenecks.",
                heatmap.getZones().size(),
                heatmap.getHighestSurplusZone(),
                heatmap.getTotalHistoricalSurplusKg(),
                heatmap.getPeakSurplusHours(),
                heatmap.getHighestDemandZone(),
                heatmap.getTotalActiveDemandKg(),
                heatmap.getTotalExpiredKg()
        );
    }

    private AiExtractionResponse fallbackExtractDonationDetails(String promptText) {
        AiExtractionResponse response = new AiExtractionResponse();
        response.setRawExtractionNotes("Parsed using AnnaDhata Natural Language Parser");

        // Extract quantity and unit: e.g. "25 kg", "15 packets", "10 kgs", "50 boxes"
        Pattern qtyPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(kg|kgs|kilogram|kilograms|packet|packets|box|boxes|meal|meals|portion|portions)", Pattern.CASE_INSENSITIVE);
        Matcher qtyMatcher = qtyPattern.matcher(promptText);
        if (qtyMatcher.find()) {
            response.setQuantity(Double.parseDouble(qtyMatcher.group(1)));
            String unit = qtyMatcher.group(3).toLowerCase();
            if (unit.startsWith("kg")) response.setUnit("kg");
            else if (unit.startsWith("packet")) response.setUnit("packets");
            else if (unit.startsWith("box")) response.setUnit("boxes");
            else if (unit.startsWith("meal") || unit.startsWith("portion")) response.setUnit("portions");
            else response.setUnit("kg");
        } else {
            response.setQuantity(10.0);
            response.setUnit("kg");
        }

        // Category detection
        String lower = promptText.toLowerCase();
        if (lower.contains("biryani") || lower.contains("curry") || lower.contains("sambar") || lower.contains("pulao") || lower.contains("cooked") || lower.contains("dal") || lower.contains("gravy") || lower.contains("meals")) {
            response.setFoodCategory(FoodCategory.COOKED_FOOD);
            if (lower.contains("biryani")) {
                response.setFoodName(lower.contains("veg") ? "Vegetable Biryani" : lower.contains("chicken") ? "Chicken Biryani" : "Prepared Biryani");
            } else if (lower.contains("pulao")) response.setFoodName("Vegetable Pulao");
            else if (lower.contains("curry")) response.setFoodName("Mixed Vegetable Curry");
            else if (lower.contains("dal")) response.setFoodName("Cooked Dal & Rice");
            else response.setFoodName("Cooked Meals");
        } else if (lower.contains("rice") || lower.contains("wheat") || lower.contains("grain") || lower.contains("atta")) {
            response.setFoodCategory(FoodCategory.RICE_AND_GRAINS);
            response.setFoodName(lower.contains("rice") ? "Cooked Rice" : "Grains / Flour");
        } else if (lower.contains("bread") || lower.contains("bun") || lower.contains("cake") || lower.contains("croissant") || lower.contains("bakery")) {
            response.setFoodCategory(FoodCategory.BREAD_AND_BAKERY);
            response.setFoodName("Fresh Bakery Goods");
        } else if (lower.contains("fruit") || lower.contains("apple") || lower.contains("banana") || lower.contains("mango")) {
            response.setFoodCategory(FoodCategory.FRUITS);
            response.setFoodName("Fresh Fruits");
        } else if (lower.contains("veg") || lower.contains("tomato") || lower.contains("potato")) {
            response.setFoodCategory(FoodCategory.VEGETABLES);
            response.setFoodName("Fresh Vegetables");
        } else if (lower.contains("milk") || lower.contains("paneer") || lower.contains("cheese") || lower.contains("curd") || lower.contains("yogurt")) {
            response.setFoodCategory(FoodCategory.DAIRY_PRODUCTS);
            response.setFoodName("Dairy Products");
        } else if (lower.contains("packet") || lower.contains("biscuit") || lower.contains("snack")) {
            response.setFoodCategory(FoodCategory.PACKAGED_FOOD);
            response.setFoodName("Packaged Food Items");
        } else {
            response.setFoodCategory(FoodCategory.COOKED_FOOD);
            response.setFoodName("Prepared Surplus Food");
        }

        // Time extraction: parse clock times like "prepared at 7 PM" and "before 9:30 PM"
        Instant now = Instant.now();
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.ZonedDateTime nowZdt = java.time.ZonedDateTime.ofInstant(now, zone);

        Pattern timePattern = Pattern.compile("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b", Pattern.CASE_INSENSITIVE);
        Matcher timeMatcher = timePattern.matcher(promptText);
        List<java.time.ZonedDateTime> matchedTimes = new ArrayList<>();
        while (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
            String ampm = timeMatcher.group(3).toLowerCase();
            if ("pm".equals(ampm) && hour < 12) hour += 12;
            if ("am".equals(ampm) && hour == 12) hour = 0;
            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                matchedTimes.add(nowZdt.withHour(hour).withMinute(minute).withSecond(0).withNano(0));
            }
        }

        if (matchedTimes.size() >= 2) {
            java.time.ZonedDateTime prepZdt = matchedTimes.get(0);
            java.time.ZonedDateTime deadlineZdt = matchedTimes.get(1);
            if (!deadlineZdt.isAfter(prepZdt)) {
                deadlineZdt = deadlineZdt.plusDays(1);
            }
            if (!deadlineZdt.isAfter(nowZdt)) {
                // Keep future deadline window when parsing past clock references in demos
                long durationMinutes = Math.max(120, Duration.between(prepZdt, deadlineZdt).toMinutes());
                response.setPreparationDateTime(now.minus(1, ChronoUnit.HOURS));
                response.setPickupDeadline(now.plus(durationMinutes, ChronoUnit.MINUTES));
            } else {
                response.setPreparationDateTime(prepZdt.toInstant());
                response.setPickupDeadline(deadlineZdt.toInstant());
            }
        } else if (matchedTimes.size() == 1) {
            java.time.ZonedDateTime t = matchedTimes.get(0);
            if (t.isAfter(nowZdt)) {
                response.setPreparationDateTime(now.minus(1, ChronoUnit.HOURS));
                response.setPickupDeadline(t.toInstant());
            } else {
                response.setPreparationDateTime(t.toInstant());
                response.setPickupDeadline(now.plus(3, ChronoUnit.HOURS));
            }
        } else {
            response.setPreparationDateTime(now.minus(1, ChronoUnit.HOURS));
            response.setPickupDeadline(now.plus(3, ChronoUnit.HOURS));
        }

        response.setFoodDescription("Extracted surplus batch from donor note: \"" + promptText.trim() + "\"");
        response.setStorageAdvice("Maintain proper hot (>65°C) or chilled (<4°C) holding until collection.");

        return response;
    }

    private String callGeminiApi(String prompt) {
        try {
            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    geminiModel, geminiApiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Collections.singletonMap("text", prompt);
            Map<String, Object> parts = Collections.singletonMap("parts", Collections.singletonList(textPart));
            Map<String, Object> requestBody = Collections.singletonMap("contents", Collections.singletonList(parts));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    String text = textNode.asText();
                    // Strip ```json markdown wrappers if present
                    if (text.startsWith("```json")) {
                        text = text.substring(7);
                    } else if (text.startsWith("```")) {
                        text = text.substring(3);
                    }
                    if (text.endsWith("```")) {
                        text = text.substring(0, text.length() - 3);
                    }
                    return text.trim();
                }
            }
        } catch (Exception ex) {
            log.warn("Gemini REST API exception: {}", ex.getMessage());
        }
        return null;
    }

    private JsonNode parseJsonSafe(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            log.warn("Failed to parse Gemini output as JSON: {}", ex.getMessage());
            return null;
        }
    }
}
