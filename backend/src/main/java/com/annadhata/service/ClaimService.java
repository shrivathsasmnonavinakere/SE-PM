package com.annadhata.service;

import com.annadhata.dto.ClaimResponse;
import com.annadhata.dto.CreateClaimRequest;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.DuplicateResourceException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final NotificationService notificationService;
    private final ImpactService impactService;
    private final PassportService passportService;
    private final TrustScoreService trustScoreService;
    private final MongoTemplate mongoTemplate;

    public ClaimService(ClaimRepository claimRepository,
                        DonationRepository donationRepository,
                        UserRepository userRepository,
                        VolunteerProfileRepository volunteerProfileRepository,
                        NotificationService notificationService,
                        ImpactService impactService,
                        PassportService passportService,
                        TrustScoreService trustScoreService,
                        MongoTemplate mongoTemplate) {
        this.claimRepository = claimRepository;
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.notificationService = notificationService;
        this.impactService = impactService;
        this.passportService = passportService;
        this.trustScoreService = trustScoreService;
        this.mongoTemplate = mongoTemplate;
    }

    public synchronized ClaimResponse createClaim(CreateClaimRequest req, User receiver) {
        Donation donation = donationRepository.findById(req.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + req.getDonationId()));

        if (donation.getDonorId().equals(receiver.getId())) {
            throw new BadRequestException("Donors cannot claim their own donations");
        }

        if (receiver.getRole() == Role.ROLE_RECEIVER && receiver.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new ForbiddenException("Your organisation must be verified by an Administrator before claiming surplus food.");
        }

        Instant now = Instant.now();
        if (donation.getPickupDeadline() != null && donation.getPickupDeadline().isBefore(now)) {
            throw new BadRequestException("Cannot claim an expired food donation");
        }

        if (donation.getSafetyIndicator() == SafetyIndicator.EXPIRED || donation.getSafetyIndicator() == SafetyIndicator.HIGH_RISK) {
            throw new BadRequestException("This listing is flagged (" + donation.getSafetyIndicator() + ") and cannot be claimed without clearance.");
        }

        if (donation.getStatus() != DonationStatus.AVAILABLE
                && donation.getStatus() != DonationStatus.MATCHED
                && donation.getStatus() != DonationStatus.PARTIALLY_CLAIMED) {
            throw new BadRequestException("This donation is no longer available (current status: " + donation.getStatus() + ")");
        }

        double availableRemaining = donation.getRemainingQuantity() != null ? donation.getRemainingQuantity()
                : (donation.getQuantity() != null ? donation.getQuantity() : 0.0);
        double requestedQty = req.getClaimedQuantity() != null ? req.getClaimedQuantity() : availableRemaining;

        if (requestedQty <= 0 || requestedQty > availableRemaining + 0.0001) {
            throw new BadRequestException(String.format("Requested quantity (%.1f) exceeds available remaining quantity (%.1f %s)",
                    requestedQty, availableRemaining, donation.getMeasurementUnit()));
        }

        // Prevent duplicate active claims by the same receiver
        claimRepository.findByDonationIdAndReceiverId(donation.getId(), receiver.getId())
                .ifPresent(c -> {
                    if (c.getStatus() == ClaimStatus.PENDING || c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PICKED_UP) {
                        throw new DuplicateResourceException("You have already submitted an active claim for this donation");
                    }
                });

        Claim claim = new Claim();
        claim.setDonationId(donation.getId());
        claim.setDonorId(donation.getDonorId());
        claim.setReceiverId(receiver.getId());
        claim.setReceiverName(receiver.getOrganizationName() != null ? receiver.getOrganizationName() : receiver.getName());
        claim.setReceiverPhone(receiver.getPhone());
        claim.setReceiverAddress(receiver.getAddress());
        claim.setClaimedQuantity(Math.round(requestedQty * 100.0) / 100.0);
        claim.setMeasurementUnit(req.getMeasurementUnit() != null ? req.getMeasurementUnit() : donation.getMeasurementUnit());
        claim.setProposedPickupTime(req.getProposedPickupTime() != null ? req.getProposedPickupTime() : now.plusSeconds(3600));
        claim.setStatus(ClaimStatus.PENDING);
        claim.setNotes(req.getNotes());
        claim.setVolunteerRequested(req.isVolunteerRequested());
        claim.setVolunteerAssignmentStatus(req.isVolunteerRequested()
                ? VolunteerAssignmentStatus.REQUESTED
                : VolunteerAssignmentStatus.UNASSIGNED);

        Claim saved = claimRepository.save(claim);

        notificationService.sendNotification(
                donation.getDonorId(),
                "New Partial/Full Claim Request!",
                String.format("%s requested %.1f %s of '%s' (remaining: %.1f %s). Proposed pickup: %s",
                        claim.getReceiverName(), claim.getClaimedQuantity(), claim.getMeasurementUnit(),
                        donation.getFoodName(), availableRemaining, donation.getMeasurementUnit(), claim.getProposedPickupTime()),
                NotificationType.CLAIM_REQUEST,
                saved.getId()
        );

        return mapToResponse(saved, donation, receiver);
    }

    public synchronized ClaimResponse approveClaim(String claimId, User donor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!claim.getDonorId().equals(donor.getId()) && donor.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to approve this claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only PENDING claims can be approved (current status: " + claim.getStatus() + ")");
        }

        double qtyToReserve = claim.getClaimedQuantity() != null ? claim.getClaimedQuantity() : 0.0;

        // Atomic decrement of remainingQuantity for Partial Food Distribution concurrency safety
        Query query = new Query(Criteria.where("id").is(claim.getDonationId())
                .and("status").in(DonationStatus.AVAILABLE, DonationStatus.MATCHED, DonationStatus.PARTIALLY_CLAIMED)
                .and("remainingQuantity").gte(qtyToReserve - 0.0001)
                .and("pickupDeadline").gt(Instant.now()));
        Update update = new Update()
                .inc("remainingQuantity", -qtyToReserve)
                .inc("allocatedQuantity", qtyToReserve)
                .addToSet("activeClaimIds", claim.getId())
                .inc("allocationCount", 1)
                .set("updatedAt", Instant.now());

        Donation updatedDonation = mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(true), Donation.class);

        if (updatedDonation == null) {
            throw new BadRequestException("Insufficient remaining food quantity or listing has expired/closed.");
        }

        double newRemaining = Math.max(0.0, Math.round((updatedDonation.getRemainingQuantity() != null ? updatedDonation.getRemainingQuantity() : 0.0) * 100.0) / 100.0);
        updatedDonation.setRemainingQuantity(newRemaining);

        if (newRemaining <= 0.01) {
            updatedDonation.setRemainingQuantity(0.0);
            updatedDonation.setStatus(DonationStatus.CLAIMED);
        } else {
            updatedDonation.setStatus(DonationStatus.PARTIALLY_CLAIMED);
        }

        int allocCount = updatedDonation.getAllocationCount();
        updatedDonation.setClaimedByReceiverId(allocCount > 1 ? "MULTIPLE" : claim.getReceiverId());
        updatedDonation.setClaimedByReceiverName(allocCount > 1
                ? allocCount + " NGOs (Partial Split)"
                : claim.getReceiverName());
        donationRepository.save(updatedDonation);

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setConfirmedPickupTime(claim.getProposedPickupTime() != null ? claim.getProposedPickupTime() : Instant.now().plusSeconds(3600));
        Claim saved = claimRepository.save(claim);

        // Auto-generate cryptographic QR Food Passport upon approval
        var passport = passportService.generatePassportForClaim(saved, updatedDonation);
        saved.setPassportId(passport.getId());
        saved.setPassportCode(passport.getPassportCode());
        saved = claimRepository.save(saved);

        // Handle remaining pending claims cleanly
        List<Claim> otherClaims = claimRepository.findByDonationId(claim.getDonationId());
        for (Claim other : otherClaims) {
            if (!other.getId().equals(saved.getId()) && other.getStatus() == ClaimStatus.PENDING) {
                double otherQty = other.getClaimedQuantity() != null ? other.getClaimedQuantity() : 0.0;
                if (newRemaining <= 0.01 || otherQty > newRemaining + 0.01) {
                    other.setStatus(ClaimStatus.REJECTED);
                    other.setRejectionReason(newRemaining <= 0.01
                            ? "Full batch has been allocated to confirmed charities."
                            : String.format("Only %.1f %s remains unallocated; please submit a smaller partial claim.",
                            newRemaining, updatedDonation.getMeasurementUnit()));
                    claimRepository.save(other);

                    notificationService.sendNotification(
                            other.getReceiverId(),
                            "Claim Update",
                            String.format("Your claim for '%s' could not be accommodated: %s",
                                    updatedDonation.getFoodName(), other.getRejectionReason()),
                            NotificationType.CLAIM_REJECTED,
                            other.getId()
                    );
                }
            }
        }

        notificationService.sendNotification(
                saved.getReceiverId(),
                "Claim Approved & QR Food Passport Issued!",
                String.format("Donor approved your %.1f %s batch of '%s'. Passport Code: %s. Pickup at %s.",
                        saved.getClaimedQuantity(), saved.getMeasurementUnit(), updatedDonation.getFoodName(),
                        saved.getPassportCode(), updatedDonation.getPickupAddress()),
                NotificationType.CLAIM_APPROVED,
                saved.getId()
        );

        try {
            trustScoreService.recalculateAndSave(donor.getId());
        } catch (Exception ex) {
            log.debug("Skipping trust score recalculation after approval: {}", ex.getMessage());
        }

        User receiverUser = userRepository.findById(saved.getReceiverId()).orElse(null);
        return mapToResponse(saved, updatedDonation, receiverUser);
    }

    public ClaimResponse rejectClaim(String claimId, String reason, User donor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!claim.getDonorId().equals(donor.getId()) && donor.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to reject this claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only PENDING claims can be rejected (current status: " + claim.getStatus() + ")");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectionReason(reason != null ? reason : "Donor could not fulfill this claim schedule.");
        Claim saved = claimRepository.save(claim);

        Donation donation = donationRepository.findById(claim.getDonationId()).orElse(null);

        notificationService.sendNotification(
                claim.getReceiverId(),
                "Claim Request Declined",
                "Your claim for '" + (donation != null ? donation.getFoodName() : "food donation") + "' was declined: " + claim.getRejectionReason(),
                NotificationType.CLAIM_REJECTED,
                saved.getId()
        );

        try {
            trustScoreService.recalculateAndSave(donor.getId());
        } catch (Exception ex) {
            log.debug("Trust recalc skipped: {}", ex.getMessage());
        }

        User receiverUser = userRepository.findById(saved.getReceiverId()).orElse(null);
        return mapToResponse(saved, donation, receiverUser);
    }

    public ClaimResponse markPickedUp(String claimId, User user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        Donation donation = donationRepository.findById(claim.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        boolean authorized = claim.getReceiverId().equals(user.getId())
                || claim.getDonorId().equals(user.getId())
                || (claim.getAssignedVolunteerId() != null && claim.getAssignedVolunteerId().equals(user.getId()))
                || user.getRole() == Role.ROLE_ADMIN;
        if (!authorized) {
            throw new ForbiddenException("Not authorized to update pickup status");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED claims can be marked as picked up (current status: " + claim.getStatus() + ")");
        }

        Instant now = Instant.now();
        claim.setStatus(ClaimStatus.PICKED_UP);
        claim.setPickedUpAt(now);
        if (claim.getAssignedVolunteerId() != null) {
            claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.PICKED_UP);
        }
        Claim savedClaim = claimRepository.save(claim);

        if (donation.getRemainingQuantity() <= 0.01) {
            List<Claim> allClaims = claimRepository.findByDonationId(donation.getId());
            boolean anyPendingPickup = allClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);
            if (!anyPendingPickup && donation.getStatus() == DonationStatus.CLAIMED) {
                donation.setStatus(DonationStatus.PICKED_UP);
                donationRepository.save(donation);
            }
        }

        notificationService.sendNotification(
                claim.getDonorId(),
                "Food Picked Up!",
                String.format("%s has collected %.1f %s of '%s'. Safe transit in progress.",
                        claim.getReceiverName(), claim.getClaimedQuantity(), claim.getMeasurementUnit(), donation.getFoodName()),
                NotificationType.PICKUP_SCHEDULED,
                savedClaim.getId()
        );

        User receiverUser = userRepository.findById(savedClaim.getReceiverId()).orElse(null);
        return mapToResponse(savedClaim, donation, receiverUser);
    }

    public ClaimResponse markCompleted(String claimId, User user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        Donation donation = donationRepository.findById(claim.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        boolean authorized = claim.getReceiverId().equals(user.getId())
                || claim.getDonorId().equals(user.getId())
                || (claim.getAssignedVolunteerId() != null && claim.getAssignedVolunteerId().equals(user.getId()))
                || user.getRole() == Role.ROLE_ADMIN;
        if (!authorized) {
            throw new ForbiddenException("Not authorized to complete this redistribution");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED && claim.getStatus() != ClaimStatus.PICKED_UP) {
            throw new BadRequestException("Only APPROVED or PICKED_UP claims can be marked completed (current status: " + claim.getStatus() + ")");
        }

        Instant now = Instant.now();
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setCompletedAt(now);
        if (claim.getPickedUpAt() == null) {
            claim.setPickedUpAt(now);
        }
        if (claim.getAssignedVolunteerId() != null) {
            claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.DELIVERED);
            VolunteerProfile vp = volunteerProfileRepository.findByUserId(claim.getAssignedVolunteerId()).orElse(null);
            if (vp != null) {
                vp.setCompletedDeliveriesCount(vp.getCompletedDeliveriesCount() + 1);
                vp.setTotalKgTransported(Math.round((vp.getTotalKgTransported() + (claim.getClaimedQuantity() != null ? claim.getClaimedQuantity() : 0.0)) * 10.0) / 10.0);
                volunteerProfileRepository.save(vp);
            }
        }
        Claim savedClaim = claimRepository.save(claim);

        // Complete whole Donation only when no remaining stock or open allocations persist
        List<Claim> allClaims = claimRepository.findByDonationId(donation.getId());
        boolean hasOpenAllocations = allClaims.stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PICKED_UP);

        if (donation.getRemainingQuantity() <= 0.01 && !hasOpenAllocations) {
            donation.setStatus(DonationStatus.COMPLETED);
            donationRepository.save(donation);
        }

        // Idempotent impact recording per claim
        impactService.recordImpact(donation, savedClaim);

        String message = String.format("Food redistribution for '%s' (%.1f %s) completed! Meals & CO2e savings recorded.",
                donation.getFoodName(), savedClaim.getClaimedQuantity(), savedClaim.getMeasurementUnit());

        notificationService.sendNotification(claim.getDonorId(), "Redistribution Completed! Impact Recorded", message, NotificationType.DONATION_COMPLETED, savedClaim.getId());
        notificationService.sendNotification(claim.getReceiverId(), "Redistribution Completed! Meals Delivered", message, NotificationType.DONATION_COMPLETED, savedClaim.getId());
        if (claim.getAssignedVolunteerId() != null) {
            notificationService.sendNotification(claim.getAssignedVolunteerId(), "Volunteer Delivery Completed!", message, NotificationType.VOLUNTEER_DELIVERED, savedClaim.getId());
        }

        try {
            trustScoreService.recalculateAndSave(claim.getDonorId());
            trustScoreService.recalculateAndSave(claim.getReceiverId());
            if (claim.getAssignedVolunteerId() != null) {
                trustScoreService.recalculateAndSave(claim.getAssignedVolunteerId());
            }
        } catch (Exception ex) {
            log.debug("Trust recalculation warning: {}", ex.getMessage());
        }

        User receiverUser = userRepository.findById(savedClaim.getReceiverId()).orElse(null);
        return mapToResponse(savedClaim, donation, receiverUser);
    }

    public List<ClaimResponse> getReceiverClaims(String receiverId) {
        return enrichClaimList(claimRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId));
    }

    public List<ClaimResponse> getDonorClaims(String donorId) {
        return enrichClaimList(claimRepository.findByDonorIdOrderByCreatedAtDesc(donorId));
    }

    public List<ClaimResponse> getDonationClaims(String donationId, User user) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));
        if (!donation.getDonorId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to view claims for this donation");
        }
        return enrichClaimList(claimRepository.findByDonationId(donationId));
    }

    public List<ClaimResponse> getDonationClaims(String donationId) {
        return enrichClaimList(claimRepository.findByDonationId(donationId));
    }

    private List<ClaimResponse> enrichClaimList(List<Claim> claims) {
        if (claims.isEmpty()) return List.of();

        Set<String> donationIds = claims.stream().map(Claim::getDonationId).collect(Collectors.toSet());
        Set<String> receiverIds = claims.stream().map(Claim::getReceiverId).collect(Collectors.toSet());

        Map<String, Donation> donationMap = donationRepository.findAllById(donationIds).stream()
                .collect(Collectors.toMap(Donation::getId, d -> d, (a, b) -> a));
        Map<String, User> receiverMap = userRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return claims.stream()
                .map(c -> mapToResponse(c, donationMap.get(c.getDonationId()), receiverMap.get(c.getReceiverId())))
                .collect(Collectors.toList());
    }

    public ClaimResponse mapToResponse(Claim c, Donation donation, User receiverUser) {
        ClaimResponse resp = new ClaimResponse();
        resp.setId(c.getId());
        resp.setDonationId(c.getDonationId());
        resp.setDonorId(c.getDonorId());
        resp.setReceiverId(c.getReceiverId());
        resp.setReceiverName(c.getReceiverName());
        resp.setReceiverPhone(c.getReceiverPhone());
        resp.setReceiverAddress(c.getReceiverAddress());
        resp.setClaimedQuantity(c.getClaimedQuantity());
        resp.setMeasurementUnit(c.getMeasurementUnit());
        resp.setProposedPickupTime(c.getProposedPickupTime());
        resp.setConfirmedPickupTime(c.getConfirmedPickupTime());
        resp.setStatus(c.getStatus());
        resp.setNotes(c.getNotes());
        resp.setRejectionReason(c.getRejectionReason());

        resp.setPassportId(c.getPassportId());
        resp.setPassportCode(c.getPassportCode());
        resp.setQrVerified(c.isQrVerified());
        resp.setQrVerifiedAt(c.getQrVerifiedAt());

        resp.setRescueRelayStatus(c.getRescueRelayStatus());
        resp.setMissedPickupDetectedAt(c.getMissedPickupDetectedAt());
        resp.setReleasedAt(c.getReleasedAt());
        resp.setRescueRelayNotes(c.getRescueRelayNotes());

        resp.setVolunteerRequested(c.isVolunteerRequested());
        resp.setVolunteerAssignmentStatus(c.getVolunteerAssignmentStatus());
        resp.setAssignedVolunteerId(c.getAssignedVolunteerId());
        resp.setAssignedVolunteerName(c.getAssignedVolunteerName());
        resp.setAssignedVolunteerPhone(c.getAssignedVolunteerPhone());

        resp.setPickedUpAt(c.getPickedUpAt());
        resp.setCompletedAt(c.getCompletedAt());
        resp.setCreatedAt(c.getCreatedAt());
        resp.setUpdatedAt(c.getUpdatedAt());

        if (donation != null) {
            resp.setFoodName(donation.getFoodName());
            resp.setDonorName(donation.getDonorName());
            resp.setDonorPhone(donation.getDonorPhone());
            resp.setPickupAddress(donation.getPickupAddress());
            resp.setDonorLatitude(donation.getLatitude());
            resp.setDonorLongitude(donation.getLongitude());
            resp.setPickupDeadline(donation.getPickupDeadline());
        } else {
            resp.setFoodName("Donation Item");
        }

        if (receiverUser != null) {
            resp.setReceiverTrustScore(receiverUser.getTrustScore());
            resp.setReceiverTrustTier(receiverUser.getTrustTier());
        }
        return resp;
    }
}
