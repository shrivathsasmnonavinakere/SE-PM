package com.annadhata.service;

import com.annadhata.dto.VolunteerDashboardResponse;
import com.annadhata.dto.VolunteerProfileUpdateRequest;
import com.annadhata.dto.VolunteerTaskResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.Role;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.exception.BadRequestException;
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
import com.annadhata.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VolunteerService {

    private static final Logger log = LoggerFactory.getLogger(VolunteerService.class);

    private final VolunteerProfileRepository volunteerProfileRepository;
    private final ClaimRepository claimRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ImpactService impactService;
    private final TrustScoreService trustScoreService;
    private final MongoTemplate mongoTemplate;

    public VolunteerService(VolunteerProfileRepository volunteerProfileRepository,
                            ClaimRepository claimRepository,
                            DonationRepository donationRepository,
                            UserRepository userRepository,
                            NotificationService notificationService,
                            ImpactService impactService,
                            TrustScoreService trustScoreService,
                            MongoTemplate mongoTemplate) {
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.claimRepository = claimRepository;
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.impactService = impactService;
        this.trustScoreService = trustScoreService;
        this.mongoTemplate = mongoTemplate;
    }

    public VolunteerProfile getOrCreateProfile(User user) {
        return volunteerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    VolunteerProfile profile = new VolunteerProfile();
                    profile.setUserId(user.getId());
                    profile.setUserName(user.getName());
                    profile.setPhone(user.getPhone());
                    profile.setLatitude(user.getLatitude() != null ? user.getLatitude() : 12.9716);
                    profile.setLongitude(user.getLongitude() != null ? user.getLongitude() : 77.5946);
                    profile.setAddress(user.getAddress() != null ? user.getAddress() : "Bengaluru");
                    return volunteerProfileRepository.save(profile);
                });
    }

    public VolunteerProfile updateProfile(VolunteerProfileUpdateRequest req, User user) {
        VolunteerProfile profile = getOrCreateProfile(user);
        if (req.getVehicleType() != null) profile.setVehicleType(req.getVehicleType());
        if (req.getMaxCapacityKg() != null) profile.setMaxCapacityKg(req.getMaxCapacityKg());
        if (req.getServiceRadiusKm() != null) profile.setServiceRadiusKm(req.getServiceRadiusKm());
        if (req.getAvailableForDispatch() != null) profile.setAvailableForDispatch(req.getAvailableForDispatch());
        if (req.getLatitude() != null) profile.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) profile.setLongitude(req.getLongitude());
        if (req.getAddress() != null) profile.setAddress(req.getAddress());
        profile.setUpdatedAt(Instant.now());
        return volunteerProfileRepository.save(profile);
    }

    public VolunteerTaskResponse requestVolunteerSupport(String claimId, User caller) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!claim.getDonorId().equals(caller.getId()) && !claim.getReceiverId().equals(caller.getId())
                && caller.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to request volunteer support for this claim");
        }
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Volunteer dispatch can only be requested for APPROVED claims");
        }
        if (claim.getAssignedVolunteerId() != null) {
            throw new BadRequestException("A volunteer is already assigned to this pickup");
        }

        claim.setVolunteerRequested(true);
        claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.REQUESTED);
        Claim saved = claimRepository.save(claim);

        Donation donation = donationRepository.findById(claim.getDonationId()).orElse(null);

        for (VolunteerProfile vp : volunteerProfileRepository.findByAvailableForDispatchTrue()) {
            notificationService.sendNotification(
                    vp.getUserId(),
                    "Volunteer Pickup Requested Nearby!",
                    String.format("%.1f %s of '%s' needs delivery from %s to %s.",
                            saved.getClaimedQuantity(), saved.getMeasurementUnit(),
                            donation != null ? donation.getFoodName() : "Food Batch",
                            donation != null ? donation.getPickupAddress() : "Donor Kitchen",
                            saved.getReceiverName()),
                    NotificationType.VOLUNTEER_ASSIGNED,
                    saved.getId()
            );
        }

        return buildTaskResponse(saved, donation, userRepository.findById(saved.getReceiverId()).orElse(null), null);
    }

    public List<VolunteerTaskResponse> getAvailableTasks(User volunteer) {
        VolunteerProfile profile = getOrCreateProfile(volunteer);
        double vLat = profile.getLatitude() != null ? profile.getLatitude() : 12.9716;
        double vLon = profile.getLongitude() != null ? profile.getLongitude() : 77.5946;

        Instant now = Instant.now();
        List<Claim> approvedClaims = claimRepository.findByStatus(ClaimStatus.APPROVED).stream()
                .filter(c -> c.getAssignedVolunteerId() == null)
                .toList();

        Set<String> donIds = approvedClaims.stream().map(Claim::getDonationId).collect(Collectors.toSet());
        Set<String> recIds = approvedClaims.stream().map(Claim::getReceiverId).collect(Collectors.toSet());

        Map<String, Donation> donMap = donationRepository.findAllById(donIds).stream()
                .collect(Collectors.toMap(Donation::getId, d -> d, (a, b) -> a));
        Map<String, User> recMap = userRepository.findAllById(recIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<VolunteerTaskResponse> tasks = new ArrayList<>();
        for (Claim c : approvedClaims) {
            Donation d = donMap.get(c.getDonationId());
            if (d == null || (d.getPickupDeadline() != null && d.getPickupDeadline().isBefore(now))) {
                continue;
            }
            VolunteerTaskResponse task = buildTaskResponse(c, d, recMap.get(c.getReceiverId()), profile);
            if (task.getDistanceFromVolunteerKm() != null && profile.getServiceRadiusKm() != null
                    && task.getDistanceFromVolunteerKm() > Math.max(40.0, profile.getServiceRadiusKm())) {
                continue;
            }
            // Prioritize explicitly requested volunteer deliveries, then nearest pickup
            tasks.add(task);
        }

        tasks.sort((a, b) -> {
            if (a.isVolunteerRequested() != b.isVolunteerRequested()) {
                return a.isVolunteerRequested() ? -1 : 1;
            }
            double dA = a.getDistanceFromVolunteerKm() != null ? a.getDistanceFromVolunteerKm() : 999.0;
            double dB = b.getDistanceFromVolunteerKm() != null ? b.getDistanceFromVolunteerKm() : 999.0;
            return Double.compare(dA, dB);
        });

        return tasks;
    }

    public List<VolunteerTaskResponse> getMyMissions(User volunteer) {
        VolunteerProfile profile = getOrCreateProfile(volunteer);
        List<Claim> claims = claimRepository.findByAssignedVolunteerIdOrderByCreatedAtDesc(volunteer.getId());
        if (claims.isEmpty()) return Collections.emptyList();

        Set<String> donIds = claims.stream().map(Claim::getDonationId).collect(Collectors.toSet());
        Set<String> recIds = claims.stream().map(Claim::getReceiverId).collect(Collectors.toSet());
        Map<String, Donation> donMap = donationRepository.findAllById(donIds).stream()
                .collect(Collectors.toMap(Donation::getId, d -> d, (a, b) -> a));
        Map<String, User> recMap = userRepository.findAllById(recIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return claims.stream()
                .map(c -> buildTaskResponse(c, donMap.get(c.getDonationId()), recMap.get(c.getReceiverId()), profile))
                .collect(Collectors.toList());
    }

    public VolunteerDashboardResponse getVolunteerDashboard(User volunteer) {
        VolunteerProfile profile = getOrCreateProfile(volunteer);
        List<VolunteerTaskResponse> openTasks = getAvailableTasks(volunteer);
        List<VolunteerTaskResponse> missions = getMyMissions(volunteer);

        List<VolunteerTaskResponse> activeMissions = missions.stream()
                .filter(m -> m.getClaimStatus() == ClaimStatus.APPROVED || m.getClaimStatus() == ClaimStatus.PICKED_UP)
                .collect(Collectors.toList());

        VolunteerDashboardResponse resp = new VolunteerDashboardResponse();
        resp.setVolunteerProfile(profile);
        resp.setOpenTasks(openTasks);
        resp.setOpenTasksCount(openTasks.size());
        resp.setActiveMissions(activeMissions);
        resp.setActiveMissionsCount(activeMissions.size());
        resp.setCompletedDeliveriesCount(profile.getCompletedDeliveriesCount());
        resp.setTotalKgTransported(profile.getTotalKgTransported());
        resp.setEstimatedMealsDelivered(Math.round(profile.getTotalKgTransported() * ImpactService.MEALS_PER_KG));
        resp.setTrustScore(volunteer.getTrustScore() != null ? volunteer.getTrustScore() : 85.0);
        resp.setTrustTier(volunteer.getTrustTier() != null ? volunteer.getTrustTier() : "GOLD");
        return resp;
    }

    public synchronized VolunteerTaskResponse acceptTask(String claimId, User volunteer) {
        if (!volunteer.isActive()) {
            throw new ForbiddenException("Suspended volunteers cannot accept delivery missions");
        }
        VolunteerProfile profile = getOrCreateProfile(volunteer);

        Query query = new Query(Criteria.where("id").is(claimId)
                .and("status").is(ClaimStatus.APPROVED)
                .and("assignedVolunteerId").isNull());
        Update update = new Update()
                .set("assignedVolunteerId", volunteer.getId())
                .set("assignedVolunteerName", volunteer.getName())
                .set("assignedVolunteerPhone", volunteer.getPhone())
                .set("volunteerAssignmentStatus", VolunteerAssignmentStatus.ASSIGNED)
                .set("volunteerAcceptedAt", Instant.now())
                .set("updatedAt", Instant.now());

        Claim assigned = mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(true), Claim.class);

        if (assigned == null) {
            throw new BadRequestException("This delivery task has already been claimed by another volunteer or is no longer open");
        }

        Donation donation = donationRepository.findById(assigned.getDonationId()).orElse(null);

        notificationService.sendNotification(
                assigned.getDonorId(),
                "Volunteer Assigned for Pickup!",
                String.format("%s (%s) accepted pickup duty for %.1f %s of '%s'. Show/scan QR Passport %s on arrival.",
                        volunteer.getName(), profile.getVehicleType(), assigned.getClaimedQuantity(),
                        assigned.getMeasurementUnit(), donation != null ? donation.getFoodName() : "food",
                        assigned.getPassportCode()),
                NotificationType.VOLUNTEER_ASSIGNED,
                assigned.getId()
        );
        notificationService.sendNotification(
                assigned.getReceiverId(),
                "Volunteer On Route to Pick Up Your Claim!",
                String.format("Volunteer %s (%s, %s) will collect your %.1f %s batch of '%s'.",
                        volunteer.getName(), volunteer.getPhone() != null ? volunteer.getPhone() : "verified",
                        profile.getVehicleType(), assigned.getClaimedQuantity(), assigned.getMeasurementUnit(),
                        donation != null ? donation.getFoodName() : "food"),
                NotificationType.VOLUNTEER_ASSIGNED,
                assigned.getId()
        );

        return buildTaskResponse(assigned, donation, userRepository.findById(assigned.getReceiverId()).orElse(null), profile);
    }

    public VolunteerTaskResponse releaseTask(String claimId, User volunteer) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!volunteer.getId().equals(claim.getAssignedVolunteerId()) && volunteer.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Not authorized to release this volunteer task");
        }
        if (claim.getStatus() != ClaimStatus.APPROVED || claim.isQrVerified()) {
            throw new BadRequestException("Cannot release a task after food pickup has started or been QR-verified");
        }

        claim.setAssignedVolunteerId(null);
        claim.setAssignedVolunteerName(null);
        claim.setAssignedVolunteerPhone(null);
        claim.setVolunteerAcceptedAt(null);
        claim.setVolunteerAssignmentStatus(claim.isVolunteerRequested()
                ? VolunteerAssignmentStatus.REQUESTED
                : VolunteerAssignmentStatus.UNASSIGNED);
        Claim saved = claimRepository.save(claim);

        Donation donation = donationRepository.findById(claim.getDonationId()).orElse(null);
        notificationService.sendNotification(
                claim.getDonorId(),
                "Volunteer Unassigned",
                String.format("Volunteer stepped back from pickup for '%s'. Task returned to open dispatch queue.",
                        donation != null ? donation.getFoodName() : "donation"),
                NotificationType.VOLUNTEER_ASSIGNED,
                claim.getId()
        );

        return buildTaskResponse(saved, donation, userRepository.findById(saved.getReceiverId()).orElse(null), null);
    }

    public VolunteerTaskResponse markDelivered(String claimId, User volunteer) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        if (!volunteer.getId().equals(claim.getAssignedVolunteerId()) && volunteer.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Only the assigned volunteer or administrator can confirm delivery");
        }
        if (claim.getStatus() != ClaimStatus.APPROVED && claim.getStatus() != ClaimStatus.PICKED_UP) {
            throw new BadRequestException("Only APPROVED or PICKED_UP missions can be delivered (current: " + claim.getStatus() + ")");
        }

        Donation donation = donationRepository.findById(claim.getDonationId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        Instant now = Instant.now();
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setCompletedAt(now);
        if (claim.getPickedUpAt() == null) claim.setPickedUpAt(now);
        claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.DELIVERED);
        Claim saved = claimRepository.save(claim);

        VolunteerProfile profile = getOrCreateProfile(volunteer);
        profile.setCompletedDeliveriesCount(profile.getCompletedDeliveriesCount() + 1);
        double qty = claim.getClaimedQuantity() != null ? claim.getClaimedQuantity() : 0.0;
        profile.setTotalKgTransported(Math.round((profile.getTotalKgTransported() + qty) * 10.0) / 10.0);
        volunteerProfileRepository.save(profile);

        // Complete donation if remaining quantity == 0 and no open claims left
        List<Claim> allClaims = claimRepository.findByDonationId(donation.getId());
        boolean anyStillOpen = allClaims.stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PICKED_UP);
        if (donation.getRemainingQuantity() <= 0.01 && !anyStillOpen) {
            donation.setStatus(DonationStatus.COMPLETED);
            donationRepository.save(donation);
        }

        impactService.recordImpact(donation, saved);

        String msg = String.format("Volunteer %s completed delivery of %.1f %s '%s' to %s!",
                volunteer.getName(), qty, saved.getMeasurementUnit(), donation.getFoodName(), saved.getReceiverName());
        notificationService.sendNotification(saved.getDonorId(), "Volunteer Delivery Completed!", msg, NotificationType.VOLUNTEER_DELIVERED, saved.getId());
        notificationService.sendNotification(saved.getReceiverId(), "Food Delivered by Volunteer!", msg, NotificationType.VOLUNTEER_DELIVERED, saved.getId());

        try {
            trustScoreService.recalculateAndSave(volunteer.getId());
            trustScoreService.recalculateAndSave(saved.getDonorId());
            trustScoreService.recalculateAndSave(saved.getReceiverId());
        } catch (Exception ex) {
            log.debug("Trust recalculation warning: {}", ex.getMessage());
        }

        return buildTaskResponse(saved, donation, userRepository.findById(saved.getReceiverId()).orElse(null), profile);
    }

    private VolunteerTaskResponse buildTaskResponse(Claim c, Donation d, User receiver, VolunteerProfile vp) {
        VolunteerTaskResponse t = new VolunteerTaskResponse();
        t.setClaimId(c.getId());
        t.setDonationId(c.getDonationId());
        t.setClaimedQuantity(c.getClaimedQuantity());
        t.setMeasurementUnit(c.getMeasurementUnit());
        t.setClaimStatus(c.getStatus());
        t.setVolunteerAssignmentStatus(c.getVolunteerAssignmentStatus());
        t.setVolunteerRequested(c.isVolunteerRequested());
        t.setDonorId(c.getDonorId());
        t.setReceiverId(c.getReceiverId());
        t.setReceiverName(c.getReceiverName());
        t.setReceiverPhone(c.getReceiverPhone());
        t.setDropoffAddress(c.getReceiverAddress());
        t.setConfirmedPickupTime(c.getConfirmedPickupTime());
        t.setPassportCode(c.getPassportCode());
        t.setQrVerified(c.isQrVerified());
        t.setAssignedVolunteerId(c.getAssignedVolunteerId());
        t.setAssignedVolunteerName(c.getAssignedVolunteerName());
        t.setAssignedVolunteerPhone(c.getAssignedVolunteerPhone());

        if (receiver != null) {
            t.setDropoffLatitude(receiver.getLatitude());
            t.setDropoffLongitude(receiver.getLongitude());
        }

        if (d != null) {
            t.setFoodName(d.getFoodName());
            t.setFoodCategory(d.getFoodCategory());
            t.setDonorName(d.getDonorName());
            t.setDonorPhone(d.getDonorPhone());
            t.setPickupAddress(d.getPickupAddress());
            t.setPickupLatitude(d.getLatitude());
            t.setPickupLongitude(d.getLongitude());
            t.setPickupDeadline(d.getPickupDeadline());

            if (vp != null && vp.getLatitude() != null && vp.getLongitude() != null
                    && d.getLatitude() != null && d.getLongitude() != null) {
                t.setDistanceFromVolunteerKm(GeoUtils.haversineDistance(vp.getLatitude(), vp.getLongitude(), d.getLatitude(), d.getLongitude()));
            }
            if (d.getLatitude() != null && d.getLongitude() != null
                    && t.getDropoffLatitude() != null && t.getDropoffLongitude() != null) {
                t.setRouteDistanceKm(GeoUtils.haversineDistance(d.getLatitude(), d.getLongitude(), t.getDropoffLatitude(), t.getDropoffLongitude()));
            }
        }
        return t;
    }
}

