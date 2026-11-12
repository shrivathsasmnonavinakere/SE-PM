package com.annadhata.service;

import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.dto.RescueRelayActionRequest;
import com.annadhata.dto.RescueRelayResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.matching.MatchingService;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.RescueRelayEvent;
import com.annadhata.model.User;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.RescueRelayEventRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RescueRelayService {

    private static final Logger log = LoggerFactory.getLogger(RescueRelayService.class);
    private static final int DEFAULT_GRACE_MINUTES = 15;

    private final ClaimRepository claimRepository;
    private final DonationRepository donationRepository;
    private final RescueRelayEventRepository relayEventRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PassportService passportService;
    private final NotificationService notificationService;
    private final MatchingService matchingService;
    private final TrustScoreService trustScoreService;
    private final MongoTemplate mongoTemplate;

    public RescueRelayService(ClaimRepository claimRepository,
                              DonationRepository donationRepository,
                              RescueRelayEventRepository relayEventRepository,
                              VolunteerProfileRepository volunteerProfileRepository,
                              PassportService passportService,
                              NotificationService notificationService,
                              MatchingService matchingService,
                              TrustScoreService trustScoreService,
                              MongoTemplate mongoTemplate) {
        this.claimRepository = claimRepository;
        this.donationRepository = donationRepository;
        this.relayEventRepository = relayEventRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passportService = passportService;
        this.notificationService = notificationService;
        this.matchingService = matchingService;
        this.trustScoreService = trustScoreService;
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduledMissedPickupScan() {
        detectMissedPickups(DEFAULT_GRACE_MINUTES);
    }

    public List<RescueRelayResponse> detectMissedPickups(int graceMinutes) {
        Instant threshold = Instant.now().minus(Math.max(0, graceMinutes), ChronoUnit.MINUTES);
        List<Claim> overdueClaims = claimRepository.findByStatusAndConfirmedPickupTimeBefore(ClaimStatus.APPROVED, threshold);

        List<RescueRelayResponse> newlyDetected = new ArrayList<>();
        for (Claim claim : overdueClaims) {
            if (claim.isQrVerified() || claim.getPickedUpAt() != null) {
                continue;
            }
            if (claim.getRescueRelayStatus() != null && claim.getRescueRelayStatus() != RescueRelayStatus.NONE) {
                continue; // Already flagged or resolved
            }

            Donation donation = donationRepository.findById(claim.getDonationId()).orElse(null);
            if (donation == null || donation.getStatus() == DonationStatus.COMPLETED
                    || donation.getStatus() == DonationStatus.CANCELLED) {
                continue;
            }

            Instant now = Instant.now();
            claim.setRescueRelayStatus(RescueRelayStatus.AWAITING_DONOR_APPROVAL);
            claim.setMissedPickupDetectedAt(now);
            claim.setRescueRelayNotes("Confirmed pickup window missed without QR Food Passport verification.");
            claimRepository.save(claim);

            RescueRelayEvent event = relayEventRepository.findByClaimId(claim.getId()).orElse(new RescueRelayEvent());
            event.setClaimId(claim.getId());
            event.setDonationId(donation.getId());
            event.setDonorId(donation.getDonorId());
            event.setOriginalReceiverId(claim.getReceiverId());
            event.setOriginalReceiverName(claim.getReceiverName());
            event.setFoodName(donation.getFoodName());
            event.setQuantityAtRisk(claim.getClaimedQuantity());
            event.setMeasurementUnit(claim.getMeasurementUnit());
            event.setStatus(RescueRelayStatus.AWAITING_DONOR_APPROVAL);
            event.setConfirmedPickupTimeSnapshot(claim.getConfirmedPickupTime());
            event.setPickupDeadlineSnapshot(donation.getPickupDeadline());
            event.setDetectedAt(now);
            event.setDonorNotifiedAt(now);
            RescueRelayEvent savedEvent = relayEventRepository.save(event);

            notificationService.sendNotification(
                    claim.getReceiverId(),
                    "URGENT: Missed Pickup Window Alert",
                    String.format("Your pickup window (%s) for %.1f %s of '%s' has passed without verification. Donor has been alerted for Rescue Relay.",
                            claim.getConfirmedPickupTime(), claim.getClaimedQuantity(), claim.getMeasurementUnit(), donation.getFoodName()),
                    NotificationType.RESCUE_RELAY_ALERT,
                    claim.getId()
            );

            notificationService.sendNotification(
                    donation.getDonorId(),
                    "Rescue Relay Action Needed: Missed Pickup Detected",
                    String.format("%s missed pickup for %.1f %s of '%s'. Open Rescue Relay to approve release and re-match to alternative NGOs.",
                            claim.getReceiverName(), claim.getClaimedQuantity(), claim.getMeasurementUnit(), donation.getFoodName()),
                    NotificationType.RESCUE_RELAY_ALERT,
                    claim.getId()
            );

            try {
                trustScoreService.recalculateAndSave(claim.getReceiverId());
            } catch (Exception ex) {
                log.debug("Trust score recalc skipped on relay detection: {}", ex.getMessage());
            }

            newlyDetected.add(mapToResponse(savedEvent, Collections.emptyList()));
        }

        return newlyDetected;
    }

    public synchronized RescueRelayResponse approveReleaseAndRematch(String claimId, RescueRelayActionRequest request, User actor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!claim.getDonorId().equals(actor.getId()) && actor.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Only the food donor or administrator can approve releasing this allocation");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Only unpicked APPROVED allocations can be released via Rescue Relay (status: " + claim.getStatus() + ")");
        }

        if (claim.isQrVerified() || claim.getPickedUpAt() != null) {
            throw new BadRequestException("Cannot release an allocation whose pickup has already been verified");
        }

        Donation donation = donationRepository.findById(claim.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + claim.getDonationId()));

        Instant now = Instant.now();
        double qtyToReturn = claim.getClaimedQuantity() != null ? claim.getClaimedQuantity() : 0.0;

        // Release claim and revoke unused QR Food Passport
        claim.setStatus(ClaimStatus.RELEASED);
        claim.setRescueRelayStatus(RescueRelayStatus.RELEASED);
        claim.setReleasedAt(now);
        String notes = request != null && request.getNotes() != null
                ? request.getNotes()
                : "Released with donor approval via Rescue Relay due to missed pickup";
        claim.setRescueRelayNotes(notes);
        if (claim.getAssignedVolunteerId() != null && claim.getVolunteerAssignmentStatus() == VolunteerAssignmentStatus.ASSIGNED) {
            notificationService.sendNotification(
                    claim.getAssignedVolunteerId(),
                    "Delivery Reassigned via Rescue Relay",
                    "The original claim on '" + donation.getFoodName() + "' was released for alternative NGO re-matching.",
                    NotificationType.RESCUE_RELAY_ALERT,
                    claim.getId()
            );
            claim.setAssignedVolunteerId(null);
            claim.setAssignedVolunteerName(null);
            claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.UNASSIGNED);
        }
        claimRepository.save(claim);

        passportService.revokePassportForClaim(claim.getId(), "Released via Rescue Relay by donor");

        // Atomically return released quantity back to Donation pool
        Query donQuery = new Query(Criteria.where("id").is(donation.getId()));
        Update donUpdate = new Update()
                .inc("remainingQuantity", qtyToReturn)
                .inc("allocatedQuantity", -qtyToReturn)
                .pull("activeClaimIds", claim.getId())
                .set("updatedAt", now);

        Donation restored = mongoTemplate.findAndModify(
                donQuery, donUpdate, FindAndModifyOptions.options().returnNew(true), Donation.class);

        if (restored != null) {
            double clampedRemaining = Math.min(
                    restored.getQuantity() != null ? restored.getQuantity() : qtyToReturn,
                    Math.max(0.0, Math.round((restored.getRemainingQuantity() != null ? restored.getRemainingQuantity() : qtyToReturn) * 100.0) / 100.0));
            double clampedAllocated = Math.max(0.0, Math.round((restored.getAllocatedQuantity() != null ? restored.getAllocatedQuantity() : 0.0) * 100.0) / 100.0);
            restored.setRemainingQuantity(clampedRemaining);
            restored.setAllocatedQuantity(clampedAllocated);
            restored.setAllocationCount(Math.max(0, restored.getAllocationCount() - 1));
            restored.setStatus(clampedAllocated > 0.01 ? DonationStatus.PARTIALLY_CLAIMED : DonationStatus.AVAILABLE);

            if (request != null && request.getExtendDeadlineMinutes() != null && request.getExtendDeadlineMinutes() > 0) {
                Instant baseTime = restored.getPickupDeadline() != null && restored.getPickupDeadline().isAfter(now)
                        ? restored.getPickupDeadline() : now;
                restored.setPickupDeadline(baseTime.plus(request.getExtendDeadlineMinutes(), ChronoUnit.MINUTES));
            } else if (restored.getPickupDeadline() != null && restored.getPickupDeadline().isBefore(now)) {
                // Give reasonable 60-minute rescue rescue window if just lapsed
                restored.setPickupDeadline(now.plus(60, ChronoUnit.MINUTES));
            }
            restored = donationRepository.save(restored);
        }

        // Gather all receivers who already missed/released this donation so we exclude them
        Set<String> excludedReceivers = new HashSet<>();
        excludedReceivers.add(claim.getReceiverId());
        relayEventRepository.findByDonationId(donation.getId()).forEach(ev -> {
            if (ev.getStatus() == RescueRelayStatus.RELEASED && ev.getOriginalReceiverId() != null) {
                excludedReceivers.add(ev.getOriginalReceiverId());
            }
        });

        List<MatchRecommendationResponse> alternatives = matchingService.findMatchesForDonation(donation.getId(), excludedReceivers);

        if (!alternatives.isEmpty() && restored != null) {
            if (restored.getStatus() == DonationStatus.AVAILABLE) {
                restored.setStatus(DonationStatus.MATCHED);
                donationRepository.save(restored);
            }
            for (MatchRecommendationResponse alt : alternatives) {
                notificationService.sendNotification(
                        alt.getReceiverId(),
                        "RESCUE RELAY: Urgent Surplus Food Released Nearby!",
                        String.format("%.1f %s of '%s' was just released %.1f km away. Claim immediately with quick pickup!",
                                qtyToReturn, claim.getMeasurementUnit(), donation.getFoodName(), alt.getDistanceKm()),
                        NotificationType.RESCUE_RELAY_ALERT,
                        donation.getId()
                );
            }
        }

        // Also alert active available volunteers for rapid pickup rescue readiness
        for (VolunteerProfile vp : volunteerProfileRepository.findByAvailableForDispatchTrue()) {
            notificationService.sendNotification(
                    vp.getUserId(),
                    "Rescue Relay: Urgent Pickup Volunteer Possibly Needed",
                    String.format("%.1f %s of '%s' at %s was released via Rescue Relay.",
                            qtyToReturn, claim.getMeasurementUnit(), donation.getFoodName(), donation.getPickupAddress()),
                    NotificationType.RESCUE_RELAY_ALERT,
                    donation.getId()
            );
        }

        notificationService.sendNotification(
                claim.getReceiverId(),
                "Allocation Released via Rescue Relay",
                String.format("Because the scheduled pickup for '%s' elapsed, the donor released %.1f %s to prevent waste.",
                        donation.getFoodName(), qtyToReturn, claim.getMeasurementUnit()),
                NotificationType.RESCUE_RELAY_ALERT,
                claim.getId()
        );

        RescueRelayEvent event = relayEventRepository.findByClaimId(claim.getId()).orElse(new RescueRelayEvent());
        event.setClaimId(claim.getId());
        event.setDonationId(donation.getId());
        event.setDonorId(donation.getDonorId());
        event.setOriginalReceiverId(claim.getReceiverId());
        event.setOriginalReceiverName(claim.getReceiverName());
        event.setFoodName(donation.getFoodName());
        event.setQuantityAtRisk(qtyToReturn);
        event.setMeasurementUnit(claim.getMeasurementUnit());
        event.setStatus(RescueRelayStatus.RELEASED);
        event.setDetectedAt(event.getDetectedAt() != null ? event.getDetectedAt() : now);
        event.setResolvedAt(now);
        event.setResolvedByUserId(actor.getId());
        event.setResolutionNotes(notes);
        event.setAlternativeReceiverIds(alternatives.stream().map(MatchRecommendationResponse::getReceiverId).collect(Collectors.toList()));
        RescueRelayEvent savedEvent = relayEventRepository.save(event);

        try {
            trustScoreService.recalculateAndSave(claim.getReceiverId());
            trustScoreService.recalculateAndSave(donation.getDonorId());
        } catch (Exception ex) {
            log.debug("Trust recalculation skipped: {}", ex.getMessage());
        }

        return mapToResponse(savedEvent, alternatives);
    }

    public RescueRelayResponse dismissRelay(String claimId, String notes, User actor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!claim.getDonorId().equals(actor.getId()) && actor.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Only the donor or admin can dismiss this Rescue Relay alert");
        }

        claim.setRescueRelayStatus(RescueRelayStatus.DISMISSED);
        claim.setRescueRelayNotes(notes != null ? notes : "Donor elected to extend waiting window for scheduled NGO.");
        claimRepository.save(claim);

        RescueRelayEvent event = relayEventRepository.findByClaimId(claimId).orElse(new RescueRelayEvent());
        event.setClaimId(claimId);
        event.setDonationId(claim.getDonationId());
        event.setDonorId(claim.getDonorId());
        event.setOriginalReceiverId(claim.getReceiverId());
        event.setOriginalReceiverName(claim.getReceiverName());
        event.setStatus(RescueRelayStatus.DISMISSED);
        event.setResolvedAt(Instant.now());
        event.setResolvedByUserId(actor.getId());
        event.setResolutionNotes(claim.getRescueRelayNotes());
        RescueRelayEvent saved = relayEventRepository.save(event);

        return mapToResponse(saved, Collections.emptyList());
    }

    public List<RescueRelayResponse> getDonorRelays(String donorId) {
        return relayEventRepository.findByDonorIdOrderByDetectedAtDesc(donorId).stream()
                .map(e -> mapToResponse(e, e.getStatus() == RescueRelayStatus.AWAITING_DONOR_APPROVAL
                        ? previewAlternatives(e)
                        : Collections.emptyList()))
                .collect(Collectors.toList());
    }

    public List<RescueRelayResponse> getReceiverRelays(String receiverId) {
        return relayEventRepository.findByOriginalReceiverIdOrderByDetectedAtDesc(receiverId).stream()
                .map(e -> mapToResponse(e, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    public List<RescueRelayResponse> getAllRelays() {
        return relayEventRepository.findAll().stream()
                .sorted((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()))
                .map(e -> mapToResponse(e, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    private List<MatchRecommendationResponse> previewAlternatives(RescueRelayEvent event) {
        try {
            return matchingService.findMatchesForDonation(
                    event.getDonationId(),
                    event.getOriginalReceiverId() != null ? Set.of(event.getOriginalReceiverId()) : Collections.emptySet()
            );
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private RescueRelayResponse mapToResponse(RescueRelayEvent e, List<MatchRecommendationResponse> alts) {
        RescueRelayResponse r = new RescueRelayResponse();
        r.setId(e.getId());
        r.setClaimId(e.getClaimId());
        r.setDonationId(e.getDonationId());
        r.setDonorId(e.getDonorId());
        r.setOriginalReceiverId(e.getOriginalReceiverId());
        r.setOriginalReceiverName(e.getOriginalReceiverName());
        r.setFoodName(e.getFoodName());
        r.setQuantityAtRisk(e.getQuantityAtRisk());
        r.setMeasurementUnit(e.getMeasurementUnit());
        r.setStatus(e.getStatus());
        r.setConfirmedPickupTimeSnapshot(e.getConfirmedPickupTimeSnapshot());
        r.setPickupDeadlineSnapshot(e.getPickupDeadlineSnapshot());
        r.setDetectedAt(e.getDetectedAt());
        r.setDonorNotifiedAt(e.getDonorNotifiedAt());
        r.setResolvedAt(e.getResolvedAt());
        r.setResolutionNotes(e.getResolutionNotes());
        r.setAlternativeReceivers(alts);
        return r;
    }
}

