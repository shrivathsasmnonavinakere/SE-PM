package com.annadhata.matching;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.Donation;
import com.annadhata.model.MatchResult;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.model.User;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.MatchResultRepository;
import com.annadhata.repository.ReceiverNeedRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final DonationRepository donationRepository;
    private final ReceiverNeedRepository receiverNeedRepository;
    private final MatchResultRepository matchResultRepository;
    private final UserRepository userRepository;
    private final GeminiAIService geminiAIService;

    public MatchingService(DonationRepository donationRepository,
                           ReceiverNeedRepository receiverNeedRepository,
                           MatchResultRepository matchResultRepository,
                           UserRepository userRepository,
                           GeminiAIService geminiAIService) {
        this.donationRepository = donationRepository;
        this.receiverNeedRepository = receiverNeedRepository;
        this.matchResultRepository = matchResultRepository;
        this.userRepository = userRepository;
        this.geminiAIService = geminiAIService;
    }

    public List<MatchRecommendationResponse> findMatchesForDonation(String donationId) {
        return findMatchesForDonation(donationId, Collections.emptySet());
    }

    /**
     * Compute top 5 receiver matches for a donation (aware of remaining partial quantity,
     * Rescue Relay excluded NGOs, and verified NGO Trust & Reliability scores) + Gemini AI explanations.
     */
    public List<MatchRecommendationResponse> findMatchesForDonation(String donationId, Set<String> excludedReceiverIds) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));

        if (donation.getStatus() == DonationStatus.EXPIRED ||
            donation.getStatus() == DonationStatus.CANCELLED ||
            donation.getStatus() == DonationStatus.COMPLETED ||
            donation.getStatus() == DonationStatus.CLAIMED) {
            return Collections.emptyList();
        }

        double effectiveRemainingQty = donation.getRemainingQuantity() != null ? donation.getRemainingQuantity()
                : (donation.getQuantity() != null ? donation.getQuantity() : 0.0);
        if (effectiveRemainingQty <= 0.001) {
            return Collections.emptyList();
        }

        List<ReceiverNeed> allNeeds = receiverNeedRepository.findByActiveTrue();
        if (allNeeds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> receiverIds = allNeeds.stream()
                .map(ReceiverNeed::getReceiverId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, User> userMap = userRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Set<String> safeExclusions = excludedReceiverIds != null ? excludedReceiverIds : Collections.emptySet();
        List<ScoredCandidate> scoredCandidates = new ArrayList<>();

        for (ReceiverNeed need : allNeeds) {
            if (need.getReceiverId() == null || need.getReceiverId().equals(donation.getDonorId())) {
                continue;
            }
            if (safeExclusions.contains(need.getReceiverId())) {
                continue; // Exclude failed/released receiver during Rescue Relay rematching
            }
            User receiverUser = userMap.get(need.getReceiverId());
            if (receiverUser == null || !receiverUser.isActive()
                    || receiverUser.getVerificationStatus() != VerificationStatus.VERIFIED) {
                continue;
            }

            double distanceKm = GeoUtils.haversineDistance(
                    donation.getLatitude(), donation.getLongitude(),
                    need.getLatitude(), need.getLongitude()
            );

            double distanceScore = calculateDistanceScore(distanceKm);          // 30%
            double quantityScore = calculateQuantityScore(effectiveRemainingQty, need); // 25%
            double urgencyScore = calculateUrgencyScore(donation, need);        // 25%
            double foodCompatScore = calculateFoodCompatibility(donation, need); // 20%

            double baseMatchScore = (distanceScore * 0.30)
                    + (quantityScore * 0.25)
                    + (urgencyScore * 0.25)
                    + (foodCompatScore * 0.20);

            // Transparent Trust & Reliability adjustment bounded [-5.0, +5.0]
            double trustScore = receiverUser.getTrustScore() != null ? receiverUser.getTrustScore() : 80.0;
            double trustBonus = Math.round(Math.max(-5.0, Math.min(5.0, (trustScore - 75.0) * 0.2)) * 10.0) / 10.0;
            double finalScore = Math.max(0.0, Math.min(100.0, Math.round((baseMatchScore + trustBonus) * 10.0) / 10.0));

            scoredCandidates.add(new ScoredCandidate(
                    donation, need, receiverUser, distanceKm, finalScore,
                    distanceScore, quantityScore, urgencyScore, foodCompatScore,
                    trustScore, receiverUser.getTrustTier(), trustBonus
            ));
        }

        List<ScoredCandidate> top5 = scoredCandidates.stream()
                .sorted(Comparator.comparingDouble(ScoredCandidate::matchScore).reversed())
                .limit(5)
                .toList();

        matchResultRepository.deleteByDonationId(donationId);

        List<MatchRecommendationResponse> results = new ArrayList<>();
        for (ScoredCandidate c : top5) {
            ReceiverNeed need = c.need();
            User receiverUser = c.receiverUser();

            String explanation = geminiAIService.explainMatchRecommendation(donation, need, c.distanceKm(), c.matchScore());
            if (c.trustBonus() >= 2.0) {
                explanation += String.format(" High reliability standing (%s: %.0f/100).", c.trustTier(), c.trustScore());
            }

            MatchRecommendationResponse resp = new MatchRecommendationResponse();
            resp.setDonationId(donationId);
            resp.setReceiverId(need.getReceiverId());
            resp.setReceiverName(need.getReceiverName());
            resp.setReceiverPhone(receiverUser.getPhone() != null ? receiverUser.getPhone() : "");
            resp.setReceiverAddress(need.getAddress());
            resp.setReceiverLatitude(need.getLatitude());
            resp.setReceiverLongitude(need.getLongitude());
            resp.setDistanceKm(c.distanceKm());
            resp.setMatchScore(c.matchScore());
            resp.setDistanceScore(c.distanceScore());
            resp.setQuantityScore(c.quantityScore());
            resp.setUrgencyScore(c.urgencyScore());
            resp.setFoodCompatibilityScore(c.foodCompatScore());
            resp.setReceiverTrustScore(c.trustScore());
            resp.setReceiverTrustTier(c.trustTier());
            resp.setTrustBonusPoints(c.trustBonus());
            resp.setRecommendationExplanation(explanation);

            MatchResult record = new MatchResult();
            record.setDonationId(donationId);
            record.setReceiverId(need.getReceiverId());
            record.setReceiverName(need.getReceiverName());
            record.setReceiverAddress(need.getAddress());
            record.setReceiverLatitude(need.getLatitude());
            record.setReceiverLongitude(need.getLongitude());
            record.setDistanceKm(c.distanceKm());
            record.setMatchScore(c.matchScore());
            record.setDistanceScore(c.distanceScore());
            record.setQuantityScore(c.quantityScore());
            record.setUrgencyScore(c.urgencyScore());
            record.setFoodCompatibilityScore(c.foodCompatScore());
            record.setRecommendationExplanation(explanation);
            matchResultRepository.save(record);

            results.add(resp);
        }

        return results;
    }

    private double calculateDistanceScore(double distanceKm) {
        if (distanceKm <= 2.0) return 100.0;
        if (distanceKm <= 5.0) return 88.0;
        if (distanceKm <= 10.0) return 72.0;
        if (distanceKm <= 20.0) return 50.0;
        if (distanceKm <= 35.0) return 25.0;
        return Math.max(5.0, 100.0 - (distanceKm * 2.5));
    }

    private double calculateQuantityScore(double remainingQty, ReceiverNeed need) {
        double capacity = need.getMaxReceivingCapacity() != null ? need.getMaxReceivingCapacity() : 50.0;
        double reqQty = need.getRequiredQuantity() != null ? need.getRequiredQuantity() : 20.0;

        if (capacity >= remainingQty) {
            double fit = Math.abs(remainingQty - reqQty) / Math.max(remainingQty, reqQty);
            return Math.max(65.0, 100.0 - (fit * 30.0));
        } else {
            // Partial food distribution: even if NGO capacity is smaller than remaining total, it can claim a partial batch!
            return Math.max(40.0, (capacity / remainingQty) * 80.0);
        }
    }

    private double calculateUrgencyScore(Donation donation, ReceiverNeed need) {
        Instant deadline = donation.getPickupDeadline();
        if (deadline == null) return 70.0;

        Instant now = Instant.now();
        if (deadline.isBefore(now)) return 0.0;

        long hoursRemaining = Duration.between(now, deadline).toHours();
        double baseScore;
        if (hoursRemaining <= 2) baseScore = 100.0;
        else if (hoursRemaining <= 4) baseScore = 90.0;
        else if (hoursRemaining <= 8) baseScore = 80.0;
        else baseScore = 70.0;

        if (!need.isPickupAvailable() && hoursRemaining <= 4) {
            baseScore = Math.max(40.0, baseScore - 20.0);
        }
        return baseScore;
    }

    private double calculateFoodCompatibility(Donation donation, ReceiverNeed need) {
        if (donation.getFoodCategory() == need.getRequiredCategory()) {
            return 100.0;
        }
        return 45.0;
    }

    private record ScoredCandidate(
            Donation donation,
            ReceiverNeed need,
            User receiverUser,
            double distanceKm,
            double matchScore,
            double distanceScore,
            double quantityScore,
            double urgencyScore,
            double foodCompatScore,
            double trustScore,
            String trustTier,
            double trustBonus
    ) {}
}
