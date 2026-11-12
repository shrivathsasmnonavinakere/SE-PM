package com.annadhata.service;

import com.annadhata.dto.AdminDashboardResponse;
import com.annadhata.dto.DonationResponse;
import com.annadhata.dto.DonorDashboardResponse;
import com.annadhata.dto.ReceiverDashboardResponse;
import com.annadhata.dto.UserProfileResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.ImpactRecordRepository;
import com.annadhata.repository.RescueRelayEventRepository;
import com.annadhata.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DonationRepository donationRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final RescueRelayEventRepository rescueRelayEventRepository;
    private final DonationService donationService;
    private final ReceiverNeedsService receiverNeedsService;
    private final AuthService authService;

    public DashboardService(DonationRepository donationRepository,
                            ClaimRepository claimRepository,
                            UserRepository userRepository,
                            ImpactRecordRepository impactRecordRepository,
                            RescueRelayEventRepository rescueRelayEventRepository,
                            DonationService donationService,
                            ReceiverNeedsService receiverNeedsService,
                            AuthService authService) {
        this.donationRepository = donationRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.impactRecordRepository = impactRecordRepository;
        this.rescueRelayEventRepository = rescueRelayEventRepository;
        this.donationService = donationService;
        this.receiverNeedsService = receiverNeedsService;
        this.authService = authService;
    }

    public DonorDashboardResponse getDonorDashboard(String donorId) {
        List<Donation> all = donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId);
        List<Claim> allDonorClaims = claimRepository.findByDonorIdOrderByCreatedAtDesc(donorId);

        long total = all.size();
        long active = all.stream().filter(d ->
                d.getStatus() == DonationStatus.AVAILABLE
                        || d.getStatus() == DonationStatus.MATCHED
                        || d.getStatus() == DonationStatus.PARTIALLY_CLAIMED).count();
        long completed = all.stream().filter(d -> d.getStatus() == DonationStatus.COMPLETED).count();

        double totalDonated = all.stream().filter(d -> d.getStatus() != DonationStatus.CANCELLED)
                .mapToDouble(d -> d.getQuantity() != null ? d.getQuantity() : 0.0).sum();
        double redistributed = allDonorClaims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.COMPLETED)
                .mapToDouble(c -> c.getClaimedQuantity() != null ? c.getClaimedQuantity() : 0.0)
                .sum();

        Map<String, Long> categoryCount = all.stream()
                .filter(d -> d.getFoodCategory() != null)
                .collect(Collectors.groupingBy(d -> d.getFoodCategory().getDisplayName(), Collectors.counting()));

        List<DonationResponse> recent = all.stream().limit(5)
                .map(d -> donationService.mapToResponse(d, null, null))
                .collect(Collectors.toList());

        List<Claim> pendingClaims = allDonorClaims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.PENDING)
                .toList();

        long activeRelays = rescueRelayEventRepository
                .findByDonorIdAndStatusOrderByDetectedAtDesc(donorId, RescueRelayStatus.AWAITING_DONOR_APPROVAL).size();

        User donor = userRepository.findById(donorId).orElse(null);

        DonorDashboardResponse resp = new DonorDashboardResponse();
        resp.setTotalDonations(total);
        resp.setActiveDonations(active);
        resp.setCompletedDonations(completed);
        resp.setPendingClaims(pendingClaims.size());
        resp.setTotalFoodDonatedKg(Math.round(totalDonated * 10.0) / 10.0);
        resp.setTotalFoodRedistributedKg(Math.round(redistributed * 10.0) / 10.0);
        resp.setEstimatedMealsSaved(Math.round(redistributed * 2.5));
        resp.setEstimatedCo2AvoidedKg(Math.round(redistributed * 2.5 * 10.0) / 10.0);
        resp.setTrustScore(donor != null ? donor.getTrustScore() : 80.0);
        resp.setTrustTier(donor != null ? donor.getTrustTier() : "GOLD");
        resp.setActiveRescueRelaysCount(activeRelays);
        resp.setRecentDonations(recent);
        resp.setDonationsByCategory(categoryCount);

        return resp;
    }

    public ReceiverDashboardResponse getReceiverDashboard(String receiverId, Double lat, Double lon) {
        List<DonationResponse> available = donationService.getAvailableDonations(null, lat, lon, null);
        List<Claim> claims = claimRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);

        long activeClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.PENDING).count();
        long pendingPickups = claims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PICKED_UP).count();
        long completedPickups = claims.stream().filter(c -> c.getStatus() == ClaimStatus.COMPLETED).count();

        double totalReceived = claims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.COMPLETED)
                .mapToDouble(c -> c.getClaimedQuantity() != null ? c.getClaimedQuantity() : 0.0)
                .sum();

        User receiver = userRepository.findById(receiverId).orElse(null);

        ReceiverDashboardResponse resp = new ReceiverDashboardResponse();
        resp.setAvailableNearbyDonations(available.size());
        resp.setActiveClaimsCount(activeClaims);
        resp.setPendingPickupsCount(pendingPickups);
        resp.setCompletedPickupsCount(completedPickups);
        resp.setTotalQuantityReceivedKg(Math.round(totalReceived * 10.0) / 10.0);
        resp.setEstimatedMealsDistributed(Math.round(totalReceived * 2.5));
        resp.setTrustScore(receiver != null ? receiver.getTrustScore() : 80.0);
        resp.setTrustTier(receiver != null ? receiver.getTrustTier() : "GOLD");
        resp.setRecommendedDonations(available.stream().limit(6).collect(Collectors.toList()));
        resp.setCurrentNeeds(receiverNeedsService.getReceiverNeeds(receiverId));

        return resp;
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalDonors = userRepository.countByRole(Role.ROLE_DONOR);
        long totalReceivers = userRepository.countByRole(Role.ROLE_RECEIVER);
        long totalVolunteers = userRepository.countByRole(Role.ROLE_VOLUNTEER);
        long verifiedOrgs = userRepository.countByRoleAndVerificationStatus(Role.ROLE_RECEIVER, VerificationStatus.VERIFIED);
        long pendingVerifications = userRepository.countByRoleAndVerificationStatus(Role.ROLE_RECEIVER, VerificationStatus.PENDING);

        List<Donation> allDonations = donationRepository.findAll();
        long totalD = allDonations.size();
        long availableD = allDonations.stream().filter(d ->
                d.getStatus() == DonationStatus.AVAILABLE
                        || d.getStatus() == DonationStatus.MATCHED
                        || d.getStatus() == DonationStatus.PARTIALLY_CLAIMED).count();
        long completedD = allDonations.stream().filter(d -> d.getStatus() == DonationStatus.COMPLETED).count();
        long expiredD = allDonations.stream().filter(d -> d.getStatus() == DonationStatus.EXPIRED).count();

        double redistributedKg = impactRecordRepository.findAll().stream()
                .mapToDouble(r -> r.getQuantityKg() != null ? r.getQuantityKg() : 0.0)
                .sum();

        double successRate = (completedD + expiredD) > 0 ? ((double) completedD / (completedD + expiredD)) * 100.0 : (completedD > 0 ? 100.0 : 0.0);

        Map<String, Long> catDist = allDonations.stream()
                .filter(d -> d.getFoodCategory() != null)
                .collect(Collectors.groupingBy(d -> d.getFoodCategory().getDisplayName(), Collectors.counting()));

        List<DonationResponse> flagged = allDonations.stream()
                .filter(d -> d.getSafetyIndicator() == SafetyIndicator.HIGH_RISK || d.getSafetyIndicator() == SafetyIndicator.REVIEW_REQUIRED)
                .limit(10)
                .map(d -> donationService.mapToResponse(d, null, null))
                .collect(Collectors.toList());

        List<UserProfileResponse> pendingOrgUsers = userRepository.findByRoleAndVerificationStatus(Role.ROLE_RECEIVER, VerificationStatus.PENDING)
                .stream().map(authService::mapToProfileResponse).collect(Collectors.toList());

        long activeRelays = rescueRelayEventRepository.findByStatusOrderByDetectedAtDesc(RescueRelayStatus.AWAITING_DONOR_APPROVAL).size();

        AdminDashboardResponse resp = new AdminDashboardResponse();
        resp.setTotalUsers(totalUsers);
        resp.setTotalDonors(totalDonors);
        resp.setTotalReceivers(totalReceivers);
        resp.setTotalVolunteers(totalVolunteers);
        resp.setVerifiedOrganisations(verifiedOrgs);
        resp.setPendingVerificationRequests(pendingVerifications);
        resp.setActiveRescueRelaysCount(activeRelays);
        resp.setTotalDonations(totalD);
        resp.setAvailableDonations(availableD);
        resp.setCompletedDonations(completedD);
        resp.setExpiredDonations(expiredD);
        resp.setTotalFoodRedistributedKg(Math.round(redistributedKg * 10.0) / 10.0);
        resp.setEstimatedMealsSaved(Math.round(redistributedKg * 2.5));
        resp.setEstimatedCo2AvoidedKg(Math.round(redistributedKg * 2.5 * 10.0) / 10.0);
        resp.setRedistributionSuccessRate(Math.round(successRate * 10.0) / 10.0);
        resp.setFoodCategoryDistribution(catDist);
        resp.setFlaggedSafetyDonations(flagged);
        resp.setPendingVerifications(pendingOrgUsers);

        return resp;
    }
}
