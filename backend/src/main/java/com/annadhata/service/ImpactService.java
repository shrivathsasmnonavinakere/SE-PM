package com.annadhata.service;

import com.annadhata.dto.ImpactSummaryResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.ImpactRecord;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.ImpactRecordRepository;
import com.annadhata.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImpactService {

    private static final Logger log = LoggerFactory.getLogger(ImpactService.class);

    // Documented conversion constants:
    // 1 meal = 0.4 kg of food (2.5 meals per 1 kg)
    public static final double MEALS_PER_KG = 2.5;
    // 2.5 kg CO2e emissions avoided per kg of food waste diverted from landfill
    public static final double CO2_KG_PER_KG_FOOD = 2.5;

    private final ImpactRecordRepository impactRecordRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    public ImpactService(ImpactRecordRepository impactRecordRepository,
                         DonationRepository donationRepository,
                         UserRepository userRepository) {
        this.impactRecordRepository = impactRecordRepository;
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
    }

    public ImpactRecord recordImpact(Donation donation, Claim claim) {
        if (claim.getId() != null) {
            Optional<ImpactRecord> existing = impactRecordRepository.findByClaimId(claim.getId());
            if (existing.isPresent()) {
                log.info("Impact already recorded for claim {}; skipping duplicate", claim.getId());
                return existing.get();
            }
        }

        double qty = claim.getClaimedQuantity() != null ? claim.getClaimedQuantity() : donation.getQuantity();
        String area = donation.getPickupAddress() != null ? donation.getPickupAddress() : "Local Area";

        ImpactRecord record = new ImpactRecord(
                donation.getId(),
                donation.getDonorId(),
                claim.getReceiverId(),
                donation.getFoodCategory(),
                qty,
                area
        );
        record.setClaimId(claim.getId());

        ImpactRecord saved = impactRecordRepository.save(record);
        log.info("Recorded redistribution impact: {} kg ({} meals saved) for donation {} claim {}",
                saved.getQuantityKg(), saved.getMealsSaved(), donation.getId(), claim.getId());
        return saved;
    }

    public ImpactSummaryResponse getPlatformImpactSummary() {
        List<ImpactRecord> records = impactRecordRepository.findAll();

        double totalRedistributedKg = records.stream()
                .mapToDouble(r -> r.getQuantityKg() != null ? r.getQuantityKg() : 0.0)
                .sum();
        totalRedistributedKg = Math.round(totalRedistributedKg * 10.0) / 10.0;

        double mealsSaved = Math.round(totalRedistributedKg * MEALS_PER_KG);
        double co2AvoidedKg = Math.round(totalRedistributedKg * CO2_KG_PER_KG_FOOD * 10.0) / 10.0;

        long completedCount = donationRepository.countByStatus(DonationStatus.COMPLETED);
        long expiredCount = donationRepository.countByStatus(DonationStatus.EXPIRED);
        long totalDonations = donationRepository.count();

        double successRate = (completedCount + expiredCount) > 0
                ? ((double) completedCount / (completedCount + expiredCount)) * 100.0
                : (completedCount > 0 ? 100.0 : 0.0);
        successRate = Math.round(successRate * 10.0) / 10.0;

        // Group by food category
        Map<String, Double> categoryKg = new HashMap<>();
        for (ImpactRecord r : records) {
            String cat = r.getFoodCategory() != null ? r.getFoodCategory().getDisplayName() : "Other";
            categoryKg.merge(cat, r.getQuantityKg() != null ? r.getQuantityKg() : 0.0, Double::sum);
        }

        // Monthly trends (past 6 months)
        List<ImpactSummaryResponse.MonthlyImpactStat> monthlyTrends = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String label = monthDate.format(monthFmt);

            double monthKg = records.stream()
                    .filter(r -> r.getCompletionDate() != null &&
                            r.getCompletionDate().getMonth() == monthDate.getMonth() &&
                            r.getCompletionDate().getYear() == monthDate.getYear())
                    .mapToDouble(r -> r.getQuantityKg() != null ? r.getQuantityKg() : 0.0)
                    .sum();

            monthlyTrends.add(new ImpactSummaryResponse.MonthlyImpactStat(label, Math.round(monthKg * 10.0) / 10.0, Math.round(monthKg * MEALS_PER_KG)));
        }

        double actualTotalDonatedKg = donationRepository.findAll().stream()
                .filter(d -> d.getStatus() != DonationStatus.CANCELLED)
                .mapToDouble(d -> d.getQuantity() != null ? d.getQuantity() : 0.0)
                .sum();
        actualTotalDonatedKg = Math.round(Math.max(actualTotalDonatedKg, totalRedistributedKg) * 10.0) / 10.0;

        ImpactSummaryResponse response = new ImpactSummaryResponse();
        response.setTotalFoodDonatedKg(actualTotalDonatedKg);
        response.setTotalFoodRedistributedKg(totalRedistributedKg);
        response.setTotalCompletedDonations(completedCount);
        response.setEstimatedMealsSaved(mealsSaved);
        response.setEstimatedCo2AvoidedKg(co2AvoidedKg);
        response.setSuccessRatePercent(successRate);
        response.setActiveDonorsCount(userRepository.countByRole(com.annadhata.enums.Role.ROLE_DONOR));
        response.setActiveReceiversCount(userRepository.countByRole(com.annadhata.enums.Role.ROLE_RECEIVER));
        response.setCategoryRedistributedKg(categoryKg);
        response.setMonthlyTrends(monthlyTrends);

        return response;
    }
}
