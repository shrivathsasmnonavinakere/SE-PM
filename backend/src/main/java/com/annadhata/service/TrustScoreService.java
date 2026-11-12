package com.annadhata.service;

import com.annadhata.dto.TrustScoreResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.TrustScore;
import com.annadhata.model.TrustScore.TrustFactor;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.TrustScoreRepository;
import com.annadhata.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrustScoreService {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final ClaimRepository claimRepository;
    private final TrustScoreRepository trustScoreRepository;

    public TrustScoreService(UserRepository userRepository,
                             DonationRepository donationRepository,
                             ClaimRepository claimRepository,
                             TrustScoreRepository trustScoreRepository) {
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
        this.claimRepository = claimRepository;
        this.trustScoreRepository = trustScoreRepository;
    }

    public TrustScoreResponse getOrCalculateTrustScore(String userId) {
        return recalculateAndSave(userId);
    }

    public TrustScoreResponse recalculateAndSave(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        TrustScore score = trustScoreRepository.findByUserId(userId).orElse(new TrustScore());
        score.setUserId(user.getId());
        score.setUserName(user.getOrganizationName() != null ? user.getOrganizationName() : user.getName());
        score.setRole(user.getRole());

        if (user.getRole() == Role.ROLE_DONOR) {
            calculateDonorTrust(user, score);
        } else if (user.getRole() == Role.ROLE_RECEIVER) {
            calculateReceiverTrust(user, score);
        } else if (user.getRole() == Role.ROLE_VOLUNTEER) {
            calculateVolunteerTrust(user, score);
        } else {
            score.setOverallScore(100.0);
            score.setTier("PLATINUM");
            score.setFactors(List.of(new TrustFactor("Platform Governance", 100, 100.0, "Verified Administrator account")));
            score.setExplanation("System governance authority.");
        }

        score.setCalculatedAt(Instant.now());
        TrustScore saved = trustScoreRepository.save(score);

        // Cache summary score & tier on User for lightweight embedding in cards
        user.setTrustScore(saved.getOverallScore());
        user.setTrustTier(saved.getTier());
        userRepository.save(user);

        return mapToResponse(saved);
    }

    private void calculateDonorTrust(User donor, TrustScore score) {
        List<Donation> donations = donationRepository.findByDonorId(donor.getId());
        List<Claim> donorClaims = claimRepository.findByDonorIdOrderByCreatedAtDesc(donor.getId());

        int total = donations.size();
        int completed = (int) donations.stream().filter(d -> d.getStatus() == DonationStatus.COMPLETED).count();
        int expired = (int) donations.stream().filter(d -> d.getStatus() == DonationStatus.EXPIRED).count();
        int cancelled = (int) donations.stream().filter(d -> d.getStatus() == DonationStatus.CANCELLED).count();
        int safetyFlags = (int) donations.stream()
                .filter(d -> d.getSafetyIndicator() == SafetyIndicator.HIGH_RISK || d.getSafetyIndicator() == SafetyIndicator.REVIEW_REQUIRED)
                .count();
        int highRiskFlags = (int) donations.stream().filter(d -> d.getSafetyIndicator() == SafetyIndicator.HIGH_RISK).count();

        int resolvedTotal = completed + expired + cancelled;
        // Bayesian smoothed completion rate (prior: 82% baseline for new donors)
        double completionScore = resolvedTotal == 0 ? 82.0 : ((completed + 1.64) / (resolvedTotal + 2.0)) * 100.0;
        completionScore = round1(clamp(completionScore, 0.0, 100.0));

        // Safety hygiene compliance (25%)
        double safetyScore = total == 0 ? 90.0 : Math.max(0.0, 100.0 - ((highRiskFlags * 25.0 + (safetyFlags - highRiskFlags) * 8.0) / total) * 100.0 / 30.0 * 10.0);
        // Cleaner deterministic penalty formula:
        if (total > 0) {
            double penalty = (highRiskFlags * 25.0 + Math.max(0, safetyFlags - highRiskFlags) * 8.0) / Math.max(1, total);
            safetyScore = round1(clamp(100.0 - penalty * 2.5, 20.0, 100.0));
        }

        // Claim responsiveness & fulfillment (25%)
        long respondedClaims = donorClaims.stream().filter(c -> c.getStatus() != ClaimStatus.PENDING).count();
        double responsivenessScore = donorClaims.isEmpty() ? 85.0 : round1(clamp(((respondedClaims + 1.7) / (donorClaims.size() + 2.0)) * 100.0, 40.0, 100.0));

        // Verification & standing (15%)
        double standingScore = donor.isActive() ? (donor.getVerificationStatus() == VerificationStatus.VERIFIED ? 100.0 : 75.0) : 20.0;

        double overall = round1((completionScore * 0.35) + (safetyScore * 0.25) + (responsivenessScore * 0.25) + (standingScore * 0.15));
        overall = clamp(overall, 0.0, 100.0);

        List<TrustFactor> factors = new ArrayList<>();
        factors.add(new TrustFactor("Redistribution Completion Rate", 35, completionScore,
                String.format("%d completed vs %d expired/cancelled listings", completed, expired + cancelled)));
        factors.add(new TrustFactor("Food Safety & Hygiene Record", 25, safetyScore,
                String.format("%d safety flags across %d listings", safetyFlags, total)));
        factors.add(new TrustFactor("NGO Claim Responsiveness", 25, responsivenessScore,
                String.format("%d of %d incoming claims reviewed", respondedClaims, donorClaims.size())));
        factors.add(new TrustFactor("Account & Verification Standing", 15, standingScore,
                donor.getVerificationStatus().name()));

        score.setCompletedCount(completed);
        score.setTotalTransactions(total);
        score.setCancelledCount(cancelled);
        score.setSafetyFlagsCount(safetyFlags);
        score.setFactors(factors);
        score.setOverallScore(overall);
        score.setTier(resolveTier(overall, total));
        score.setExplanation(String.format("Donor score %.1f/100 (%s): %d completed redistributions, %d high-risk safety flags.",
                overall, score.getTier(), completed, highRiskFlags));
    }

    private void calculateReceiverTrust(User receiver, TrustScore score) {
        List<Claim> claims = claimRepository.findByReceiverIdOrderByCreatedAtDesc(receiver.getId());

        int totalClaims = claims.size();
        int completed = (int) claims.stream().filter(c -> c.getStatus() == ClaimStatus.COMPLETED).count();
        int approvedOrActive = (int) claims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PICKED_UP
                        || c.getStatus() == ClaimStatus.COMPLETED || c.getStatus() == ClaimStatus.RELEASED)
                .count();
        int qrOnTime = (int) claims.stream()
                .filter(c -> c.isQrVerified() || c.getStatus() == ClaimStatus.COMPLETED)
                .count();
        int missedPickups = (int) claims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.RELEASED
                        || c.getRescueRelayStatus() == RescueRelayStatus.RELEASED
                        || c.getRescueRelayStatus() == RescueRelayStatus.AWAITING_DONOR_APPROVAL
                        || c.getRescueRelayStatus() == RescueRelayStatus.MISSED_DETECTED)
                .count();
        int cancelledByReceiver = (int) claims.stream().filter(c -> c.getStatus() == ClaimStatus.CANCELLED).count();

        // On-Time Pickup & QR Handover Rate (40%)
        double onTimeScore = approvedOrActive == 0 ? 82.0 : round1(clamp(((qrOnTime + 1.64) / (approvedOrActive + 2.0)) * 100.0, 20.0, 100.0));

        // Zero Missed Pickups / Rescue Relay Record (25%)
        double noShowScore = approvedOrActive == 0 ? 90.0 : round1(clamp(100.0 - (missedPickups * 28.0), 0.0, 100.0));

        // Claim Completion Rate (20%)
        double completionScore = approvedOrActive == 0 ? 82.0 : round1(clamp(((completed + 1.64) / (approvedOrActive + 2.0)) * 100.0, 20.0, 100.0));

        // Verification & Credential Standing (15%)
        double verificationScore = receiver.getVerificationStatus() == VerificationStatus.VERIFIED ? 100.0
                : (receiver.getVerificationStatus() == VerificationStatus.PENDING ? 55.0 : 20.0);

        double overall = round1((onTimeScore * 0.40) + (noShowScore * 0.25) + (completionScore * 0.20) + (verificationScore * 0.15));
        overall = clamp(overall, 0.0, 100.0);

        List<TrustFactor> factors = new ArrayList<>();
        factors.add(new TrustFactor("On-Time Pickup & QR Handover", 40, onTimeScore,
                String.format("%d verified pickups across %d approved allocations", qrOnTime, approvedOrActive)));
        factors.add(new TrustFactor("Zero Missed-Pickup Record", 25, noShowScore,
                String.format("%d missed pickup alerts detected", missedPickups)));
        factors.add(new TrustFactor("Distribution Completion Rate", 20, completionScore,
                String.format("%d completed redistributions", completed)));
        factors.add(new TrustFactor("NGO Verification Standing", 15, verificationScore,
                "Status: " + receiver.getVerificationStatus().name()));

        score.setCompletedCount(completed);
        score.setTotalTransactions(totalClaims);
        score.setOnTimePickups(qrOnTime);
        score.setMissedPickups(missedPickups);
        score.setCancelledCount(cancelledByReceiver);
        score.setFactors(factors);
        score.setOverallScore(overall);
        score.setTier(resolveTier(overall, totalClaims));
        score.setExplanation(String.format("NGO score %.1f/100 (%s): %d verified pickups, %d missed pickup flags, %s credentials.",
                overall, score.getTier(), qrOnTime, missedPickups, receiver.getVerificationStatus()));
    }

    private void calculateVolunteerTrust(User volunteer, TrustScore score) {
        List<Claim> missions = claimRepository.findByAssignedVolunteerIdOrderByCreatedAtDesc(volunteer.getId());
        int total = missions.size();
        int completed = (int) missions.stream().filter(c -> c.getStatus() == ClaimStatus.COMPLETED).count();
        int qrVerified = (int) missions.stream().filter(Claim::isQrVerified).count();
        int missed = (int) missions.stream().filter(c -> c.getStatus() == ClaimStatus.RELEASED).count();

        double completionScore = total == 0 ? 85.0 : round1(clamp(((completed + 1.7) / (total + 2.0)) * 100.0, 20.0, 100.0));
        double qrHandoverScore = total == 0 ? 88.0 : round1(clamp(((qrVerified + 1.75) / (total + 2.0)) * 100.0, 30.0, 100.0));
        double standingScore = volunteer.isActive() ? 100.0 - Math.min(50.0, missed * 15.0) : 20.0;

        double overall = round1((completionScore * 0.50) + (qrHandoverScore * 0.30) + (standingScore * 0.20));
        overall = clamp(overall, 0.0, 100.0);

        List<TrustFactor> factors = new ArrayList<>();
        factors.add(new TrustFactor("Delivery Mission Completion", 50, completionScore,
                String.format("%d deliveries completed out of %d assigned", completed, total)));
        factors.add(new TrustFactor("QR Food Passport Verification", 30, qrHandoverScore,
                String.format("%d QR-verified handovers", qrVerified)));
        factors.add(new TrustFactor("Reliability Standing", 20, standingScore,
                String.format("%d released/missed tasks", missed)));

        score.setCompletedCount(completed);
        score.setTotalTransactions(total);
        score.setOnTimePickups(qrVerified);
        score.setMissedPickups(missed);
        score.setFactors(factors);
        score.setOverallScore(overall);
        score.setTier(resolveTier(overall, total));
        score.setExplanation(String.format("Volunteer score %.1f/100 (%s): %d missions completed cleanly with %d QR handovers.",
                overall, score.getTier(), completed, qrVerified));
    }

    private String resolveTier(double score, int txCount) {
        if (txCount == 0 && score >= 75.0) return "NEW_VERIFIED";
        if (score >= 92.0) return "PLATINUM";
        if (score >= 80.0) return "GOLD";
        if (score >= 65.0) return "SILVER";
        return "NEEDS_IMPROVEMENT";
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public TrustScoreResponse mapToResponse(TrustScore s) {
        TrustScoreResponse r = new TrustScoreResponse();
        r.setUserId(s.getUserId());
        r.setUserName(s.getUserName());
        r.setRole(s.getRole());
        r.setOverallScore(s.getOverallScore() != null ? s.getOverallScore() : 80.0);
        r.setTier(s.getTier());
        r.setFactors(s.getFactors());
        r.setCompletedCount(s.getCompletedCount() != null ? s.getCompletedCount() : 0);
        r.setTotalTransactions(s.getTotalTransactions() != null ? s.getTotalTransactions() : 0);
        r.setOnTimePickups(s.getOnTimePickups() != null ? s.getOnTimePickups() : 0);
        r.setMissedPickups(s.getMissedPickups() != null ? s.getMissedPickups() : 0);
        r.setCancelledCount(s.getCancelledCount() != null ? s.getCancelledCount() : 0);
        r.setSafetyFlagsCount(s.getSafetyFlagsCount() != null ? s.getSafetyFlagsCount() : 0);
        r.setExplanation(s.getExplanation());
        r.setCalculatedAt(s.getCalculatedAt());
        return r;
    }
}

