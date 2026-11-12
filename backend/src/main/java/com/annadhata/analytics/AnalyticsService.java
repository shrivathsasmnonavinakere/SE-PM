package com.annadhata.analytics;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiAnalyticsReportResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.model.Donation;
import com.annadhata.model.ImpactRecord;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.ImpactRecordRepository;
import com.annadhata.repository.ReceiverNeedRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final DonationRepository donationRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final ReceiverNeedRepository receiverNeedRepository;
    private final GeminiAIService geminiAIService;

    public AnalyticsService(DonationRepository donationRepository,
                            ImpactRecordRepository impactRecordRepository,
                            ReceiverNeedRepository receiverNeedRepository,
                            GeminiAIService geminiAIService) {
        this.donationRepository = donationRepository;
        this.impactRecordRepository = impactRecordRepository;
        this.receiverNeedRepository = receiverNeedRepository;
        this.geminiAIService = geminiAIService;
    }

    public AiAnalyticsReportResponse generateAggregatedReport() {
        List<Donation> allDonations = donationRepository.findAll();
        List<ImpactRecord> allImpact = impactRecordRepository.findAll();
        List<ReceiverNeed> activeNeeds = receiverNeedRepository.findByActiveTrue();

        long completedCount = allDonations.stream().filter(d -> d.getStatus() == DonationStatus.COMPLETED).count();
        long expiredCount = allDonations.stream().filter(d -> d.getStatus() == DonationStatus.EXPIRED).count();

        double totalRedistributedKg = allImpact.stream()
                .mapToDouble(r -> r.getQuantityKg() != null ? r.getQuantityKg() : 0.0)
                .sum();
        totalRedistributedKg = Math.round(totalRedistributedKg * 10.0) / 10.0;

        double successRate = (completedCount + expiredCount) > 0
                ? ((double) completedCount / (completedCount + expiredCount)) * 100.0
                : (completedCount > 0 ? 100.0 : 0.0);
        successRate = Math.round(successRate * 10.0) / 10.0;

        Map<String, Long> categoryMap = allDonations.stream()
                .filter(d -> d.getFoodCategory() != null)
                .collect(Collectors.groupingBy(d -> d.getFoodCategory().getDisplayName(), Collectors.counting()));

        String topCategory = categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Cooked food");

        // Compute real surplus vs demand per category
        Map<FoodCategory, Double> activeSupplyKg = new EnumMap<>(FoodCategory.class);
        for (Donation d : allDonations) {
            if (d.getFoodCategory() != null && (d.getStatus() == DonationStatus.AVAILABLE
                    || d.getStatus() == DonationStatus.MATCHED)) {
                activeSupplyKg.merge(d.getFoodCategory(), d.getQuantity() != null ? d.getQuantity() : 0.0, Double::sum);
            }
        }
        Map<FoodCategory, Double> activeDemandKg = new EnumMap<>(FoodCategory.class);
        for (ReceiverNeed n : activeNeeds) {
            if (n.getRequiredCategory() != null) {
                activeDemandKg.merge(n.getRequiredCategory(), n.getRequiredQuantity() != null ? n.getRequiredQuantity() : 0.0, Double::sum);
            }
        }

        String highestDeficitCategory = activeDemandKg.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue() - activeSupplyKg.getOrDefault(e.getKey(), 0.0)))
                .filter(e -> (e.getValue() - activeSupplyKg.getOrDefault(e.getKey(), 0.0)) > 0)
                .map(e -> e.getKey().getDisplayName())
                .orElse("Balanced across active categories");

        // Compute real top neighborhood / area from donation addresses
        Map<String, Long> areaCounts = new HashMap<>();
        for (Donation d : allDonations) {
            String area = extractNeighborhood(d.getPickupAddress());
            areaCounts.merge(area, 1L, Long::sum);
        }
        String topArea = areaCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No active areas recorded");

        // Compute real peak donation hour window
        Map<Integer, Long> hourBucketCounts = new HashMap<>();
        ZoneId zone = ZoneId.systemDefault();
        for (Donation d : allDonations) {
            Instant ts = d.getPreparationDateTime() != null ? d.getPreparationDateTime() : d.getCreatedAt();
            if (ts != null) {
                int hour = ZonedDateTime.ofInstant(ts, zone).getHour();
                int windowStart = (hour / 3) * 3;
                hourBucketCounts.merge(windowStart, 1L, Long::sum);
            }
        }
        String peakHours = hourBucketCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00 - %02d:00 (%d listings)", e.getKey(), (e.getKey() + 3) % 24, e.getValue()))
                .orElse("Insufficient timing data");

        String frequentlyExpiredCategory = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.EXPIRED && d.getFoodCategory() != null)
                .collect(Collectors.groupingBy(d -> d.getFoodCategory().getDisplayName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRedistributedKg", totalRedistributedKg);
        metrics.put("totalDonations", (long) allDonations.size());
        metrics.put("completedDonations", completedCount);
        metrics.put("expiredDonations", expiredCount);
        metrics.put("successRate", successRate);
        metrics.put("topCategory", topCategory);
        metrics.put("deficitCategory", highestDeficitCategory);
        metrics.put("topArea", topArea);
        metrics.put("peakHours", peakHours);
        metrics.put("frequentlyExpiredCategory", frequentlyExpiredCategory);
        metrics.put("activeDemandKg", activeDemandKg.values().stream().mapToDouble(Double::doubleValue).sum());
        metrics.put("activeSupplyKg", activeSupplyKg.values().stream().mapToDouble(Double::doubleValue).sum());

        AiAnalyticsReportResponse report = geminiAIService.generateAnalyticsReport(metrics);
        report.setCategoryDistribution(categoryMap);

        return report;
    }

    private String extractNeighborhood(String address) {
        if (address == null || address.isBlank()) return "Unspecified Zone";
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return parts[0].trim();
    }
}
