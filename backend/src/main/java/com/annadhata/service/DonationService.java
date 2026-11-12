package com.annadhata.service;

import com.annadhata.dto.CreateDonationRequest;
import com.annadhata.dto.DonationResponse;
import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.dto.UpdateDonationRequest;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.Role;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.matching.MatchingService;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final FoodSafetyService foodSafetyService;
    private final MatchingService matchingService;
    private final NotificationService notificationService;
    private final PassportService passportService;

    public DonationService(DonationRepository donationRepository,
                           ClaimRepository claimRepository,
                           UserRepository userRepository,
                           FoodSafetyService foodSafetyService,
                           MatchingService matchingService,
                           NotificationService notificationService,
                           PassportService passportService) {
        this.donationRepository = donationRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.foodSafetyService = foodSafetyService;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
        this.passportService = passportService;
    }

    public DonationResponse createDonation(CreateDonationRequest req, User donor) {
        Instant now = Instant.now();
        if (req.getPickupDeadline() == null || req.getPickupDeadline().isBefore(now)) {
            throw new BadRequestException("Pickup deadline must be in the future");
        }

        if (req.getPreparationDateTime() != null && req.getPreparationDateTime().isAfter(req.getPickupDeadline())) {
            throw new BadRequestException("Preparation time cannot be after pickup deadline");
        }

        Donation donation = new Donation();
        donation.setDonorId(donor.getId());
        donation.setDonorName(donor.getOrganizationName() != null ? donor.getOrganizationName() : donor.getName());
        donation.setDonorPhone(donor.getPhone());
        donation.setFoodName(req.getFoodName());
        donation.setFoodDescription(req.getFoodDescription());
        donation.setFoodCategory(req.getFoodCategory());
        donation.setQuantity(req.getQuantity());
        donation.setRemainingQuantity(req.getQuantity());
        donation.setAllocatedQuantity(0.0);
        donation.setAllocationCount(0);
        donation.setMeasurementUnit(req.getMeasurementUnit() != null ? req.getMeasurementUnit() : "kg");
        donation.setFoodImage(req.getFoodImage());
        donation.setPreparationDateTime(req.getPreparationDateTime() != null ? req.getPreparationDateTime() : now);
        donation.setPickupDeadline(req.getPickupDeadline());
        donation.setPickupAddress(req.getPickupAddress() != null ? req.getPickupAddress() : donor.getAddress());
        donation.setLatitude(req.getLatitude() != null ? req.getLatitude() : donor.getLatitude());
        donation.setLongitude(req.getLongitude() != null ? req.getLongitude() : donor.getLongitude());
        donation.setFoodHandlingDetails(req.getFoodHandlingDetails());
        donation.setStorageInformation(req.getStorageInformation());
        donation.setStatus(DonationStatus.AVAILABLE);

        // Preliminary Food Safety Screening
        foodSafetyService.screenDonation(donation);

        Donation saved = donationRepository.save(donation);

        // Run smart matching and notify top receivers in background/async
        try {
            List<MatchRecommendationResponse> matches = matchingService.findMatchesForDonation(saved.getId());
            if (!matches.isEmpty()) {
                saved.setStatus(DonationStatus.MATCHED);
                saved = donationRepository.save(saved);

                for (MatchRecommendationResponse match : matches) {
                    notificationService.sendNotification(
                            match.getReceiverId(),
                            "New Food Donation Available Nearby!",
                            String.format("'%s' (%s, %.1f %s) is available %.1f km away from you. Pickup before %s.",
                                    saved.getFoodName(), saved.getFoodCategory().getDisplayName(), saved.getQuantity(),
                                    saved.getMeasurementUnit(), match.getDistanceKm(), saved.getPickupDeadline()),
                            NotificationType.MATCH_RECOMMENDATION,
                            saved.getId()
                    );
                }
            }
        } catch (Exception ex) {
            log.warn("Matching calculation during creation failed: {}. Donation remains available.", ex.getMessage());
        }

        return mapToResponse(saved, null, null);
    }

    public DonationResponse updateDonation(String donationId, UpdateDonationRequest req, User user) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));

        if (!donation.getDonorId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to update this donation");
        }

        if (donation.getStatus() == DonationStatus.CLAIMED ||
            donation.getStatus() == DonationStatus.PICKUP_SCHEDULED ||
            donation.getStatus() == DonationStatus.PICKED_UP ||
            donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BadRequestException("Cannot edit a donation that has already been claimed or completed");
        }

        if (req.getFoodName() != null) donation.setFoodName(req.getFoodName());
        if (req.getFoodDescription() != null) donation.setFoodDescription(req.getFoodDescription());
        if (req.getFoodCategory() != null) donation.setFoodCategory(req.getFoodCategory());
        if (req.getQuantity() != null) {
            double allocated = donation.getAllocatedQuantity();
            if (req.getQuantity() < allocated) {
                throw new BadRequestException("Quantity cannot be reduced below already allocated amount (" + allocated + ")");
            }
            donation.setQuantity(req.getQuantity());
            donation.setRemainingQuantity(Math.round((req.getQuantity() - allocated) * 100.0) / 100.0);
        }
        if (req.getMeasurementUnit() != null) donation.setMeasurementUnit(req.getMeasurementUnit());
        if (req.getFoodImage() != null) donation.setFoodImage(req.getFoodImage());
        if (req.getPreparationDateTime() != null) donation.setPreparationDateTime(req.getPreparationDateTime());
        if (req.getPickupDeadline() != null) {
            if (req.getPickupDeadline().isBefore(Instant.now())) {
                throw new BadRequestException("Updated pickup deadline must be in the future");
            }
            donation.setPickupDeadline(req.getPickupDeadline());
        }
        if (donation.getPreparationDateTime() != null && donation.getPickupDeadline() != null
                && donation.getPreparationDateTime().isAfter(donation.getPickupDeadline())) {
            throw new BadRequestException("Preparation time cannot be after pickup deadline");
        }
        if (req.getPickupAddress() != null) donation.setPickupAddress(req.getPickupAddress());
        if (req.getLatitude() != null) donation.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) donation.setLongitude(req.getLongitude());
        if (req.getFoodHandlingDetails() != null) donation.setFoodHandlingDetails(req.getFoodHandlingDetails());
        if (req.getStorageInformation() != null) donation.setStorageInformation(req.getStorageInformation());
        if (req.getStatus() != null) {
            if (req.getStatus() == DonationStatus.CLAIMED || req.getStatus() == DonationStatus.PICKED_UP
                    || req.getStatus() == DonationStatus.COMPLETED || req.getStatus() == DonationStatus.PICKUP_SCHEDULED) {
                throw new BadRequestException("Cannot bypass claim lifecycle by setting status directly to " + req.getStatus());
            }
            donation.setStatus(req.getStatus());
        }

        foodSafetyService.screenDonation(donation);

        Donation saved = donationRepository.save(donation);
        return mapToResponse(saved, null, null);
    }

    public DonationResponse cancelDonation(String donationId, User user) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));

        if (!donation.getDonorId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to cancel this donation");
        }

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel an already completed donation");
        }

        donation.setStatus(DonationStatus.CANCELLED);
        Donation saved = donationRepository.save(donation);

        // Cascade cancel open claims and notify affected receivers
        List<Claim> openClaims = claimRepository.findByDonationId(donationId);
        for (Claim c : openClaims) {
            if (c.getStatus() == ClaimStatus.PENDING || c.getStatus() == ClaimStatus.APPROVED) {
                c.setStatus(ClaimStatus.CANCELLED);
                c.setRejectionReason("Donation listing was cancelled by donor/admin");
                claimRepository.save(c);
                passportService.revokePassportForClaim(c.getId(), "Listing cancelled");

                notificationService.sendNotification(
                        c.getReceiverId(),
                        "Donation Cancelled",
                        String.format("The food listing '%s' was cancelled, so your active claim was closed.", saved.getFoodName()),
                        NotificationType.CLAIM_REJECTED,
                        c.getId()
                );
            }
        }

        return mapToResponse(saved, null, null);
    }

    public DonationResponse getDonationById(String donationId, Double userLat, Double userLon) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));
        return mapToResponse(donation, userLat, userLon);
    }

    public List<DonationResponse> getAvailableDonations(FoodCategory category, Double userLat, Double userLon, Double maxDistanceKm) {
        Instant now = Instant.now();
        List<Donation> list = new ArrayList<>();
        List<DonationStatus> activeStatuses = List.of(
                DonationStatus.AVAILABLE,
                DonationStatus.MATCHED,
                DonationStatus.PARTIALLY_CLAIMED
        );

        for (DonationStatus status : activeStatuses) {
            if (category != null) {
                list.addAll(donationRepository.findByStatusAndFoodCategoryAndPickupDeadlineAfterOrderByPickupDeadlineAsc(status, category, now));
            } else {
                list.addAll(donationRepository.findByStatusAndPickupDeadlineAfterOrderByPickupDeadlineAsc(status, now));
            }
        }

        list = list.stream()
                .filter(d -> (d.getRemainingQuantity() != null ? d.getRemainingQuantity() : d.getQuantity()) > 0.01)
                .collect(Collectors.toList());

        Map<String, User> donorMap = loadDonorMap(list);

        return list.stream()
                .map(d -> mapToResponse(d, userLat, userLon, donorMap.get(d.getDonorId())))
                .filter(res -> {
                    if (maxDistanceKm != null && userLat != null && userLon != null) {
                        return res.getDistanceKm() != null && res.getDistanceKm() <= maxDistanceKm;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if (a.getDistanceKm() != null && b.getDistanceKm() != null) {
                        return Double.compare(a.getDistanceKm(), b.getDistanceKm());
                    }
                    return a.getPickupDeadline().compareTo(b.getPickupDeadline());
                })
                .collect(Collectors.toList());
    }

    public List<DonationResponse> getDonorDonations(String donorId) {
        List<Donation> list = donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId);
        Map<String, User> donorMap = loadDonorMap(list);
        return list.stream()
                .map(d -> mapToResponse(d, null, null, donorMap.get(d.getDonorId())))
                .collect(Collectors.toList());
    }

    public List<DonationResponse> getAllDonations() {
        List<Donation> list = donationRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
        Map<String, User> donorMap = loadDonorMap(list);
        return list.stream()
                .map(d -> mapToResponse(d, null, null, donorMap.get(d.getDonorId())))
                .collect(Collectors.toList());
    }

    private Map<String, User> loadDonorMap(List<Donation> donations) {
        if (donations.isEmpty()) return Map.of();
        Set<String> donorIds = donations.stream().map(Donation::getDonorId).filter(id -> id != null).collect(Collectors.toSet());
        return userRepository.findAllById(donorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    @Scheduled(fixedRate = 60000) // check every 60 seconds
    public void checkAndExpireOverdueDonations() {
        Instant now = Instant.now();
        List<Donation> expiredList = donationRepository.findExpiredAvailableDonations(now);
        for (Donation d : expiredList) {
            d.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(d);
            notificationService.sendNotification(
                    d.getDonorId(),
                    "Food Listing Expired",
                    String.format("Your listing '%s' has expired as the pickup deadline (%s) has passed.",
                            d.getFoodName(), d.getPickupDeadline()),
                    NotificationType.EXPIRY_WARNING,
                    d.getId()
            );
            log.info("Expired overdue donation listing: {}", d.getId());
        }
    }

    public DonationResponse mapToResponse(Donation d, Double userLat, Double userLon) {
        User donor = d.getDonorId() != null ? userRepository.findById(d.getDonorId()).orElse(null) : null;
        return mapToResponse(d, userLat, userLon, donor);
    }

    public DonationResponse mapToResponse(Donation d, Double userLat, Double userLon, User donor) {
        DonationResponse resp = new DonationResponse();
        resp.setId(d.getId());
        resp.setDonorId(d.getDonorId());
        resp.setDonorName(d.getDonorName());
        resp.setDonorPhone(d.getDonorPhone());
        resp.setFoodName(d.getFoodName());
        resp.setFoodDescription(d.getFoodDescription());
        resp.setFoodCategory(d.getFoodCategory());
        resp.setQuantity(d.getQuantity());

        double totalQty = d.getQuantity() != null ? d.getQuantity() : 0.0;
        double remQty = d.getRemainingQuantity() != null ? d.getRemainingQuantity() : totalQty;
        double allocQty = d.getAllocatedQuantity() != null ? d.getAllocatedQuantity() : Math.max(0.0, totalQty - remQty);
        resp.setRemainingQuantity(Math.round(remQty * 100.0) / 100.0);
        resp.setAllocatedQuantity(Math.round(allocQty * 100.0) / 100.0);
        resp.setAllocationPercentage(totalQty > 0 ? Math.round((allocQty / totalQty) * 1000.0) / 10.0 : 0.0);
        resp.setAllocationCount(d.getAllocationCount());

        resp.setMeasurementUnit(d.getMeasurementUnit());
        resp.setFoodImage(d.getFoodImage());
        resp.setPreparationDateTime(d.getPreparationDateTime());
        resp.setPickupDeadline(d.getPickupDeadline());
        resp.setPickupAddress(d.getPickupAddress());
        resp.setLatitude(d.getLatitude());
        resp.setLongitude(d.getLongitude());
        resp.setFoodHandlingDetails(d.getFoodHandlingDetails());
        resp.setStorageInformation(d.getStorageInformation());
        resp.setStatus(d.getStatus());
        resp.setSafetyIndicator(d.getSafetyIndicator());
        resp.setSafetyNotes(d.getSafetyNotes());
        resp.setClaimedByReceiverId(d.getClaimedByReceiverId());
        resp.setClaimedByReceiverName(d.getClaimedByReceiverName());
        resp.setCreatedAt(d.getCreatedAt());
        resp.setUpdatedAt(d.getUpdatedAt());

        if (donor != null) {
            resp.setDonorTrustScore(donor.getTrustScore());
            resp.setDonorTrustTier(donor.getTrustTier());
        } else {
            resp.setDonorTrustScore(80.0);
            resp.setDonorTrustTier("GOLD");
        }

        if (userLat != null && userLon != null && d.getLatitude() != null && d.getLongitude() != null) {
            resp.setDistanceKm(GeoUtils.haversineDistance(userLat, userLon, d.getLatitude(), d.getLongitude()));
        }

        return resp;
    }
}
