package com.annadhata;

import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.dto.RescueRelayActionRequest;
import com.annadhata.dto.RescueRelayResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.matching.MatchingService;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.RescueRelayEvent;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.RescueRelayEventRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import com.annadhata.service.NotificationService;
import com.annadhata.service.PassportService;
import com.annadhata.service.RescueRelayService;
import com.annadhata.service.TrustScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueRelayServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private RescueRelayEventRepository relayEventRepository;
    @Mock
    private VolunteerProfileRepository volunteerProfileRepository;
    @Mock
    private PassportService passportService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MatchingService matchingService;
    @Mock
    private TrustScoreService trustScoreService;
    @Mock
    private MongoTemplate mongoTemplate;

    private RescueRelayService rescueRelayService;

    @BeforeEach
    void setUp() {
        rescueRelayService = new RescueRelayService(
                claimRepository, donationRepository, relayEventRepository, volunteerProfileRepository,
                passportService, notificationService, matchingService, trustScoreService, mongoTemplate
        );
    }

    @Test
    @DisplayName("Rescue Relay detects missed pickup past grace period and alerts both receiver and donor")
    void testDetectMissedPickups() {
        Claim overdue = new Claim();
        overdue.setId("clm-overdue");
        overdue.setDonationId("don-1");
        overdue.setDonorId("donor-1");
        overdue.setReceiverId("rec-late");
        overdue.setReceiverName("Late Shelter");
        overdue.setClaimedQuantity(20.0);
        overdue.setMeasurementUnit("kg");
        overdue.setStatus(ClaimStatus.APPROVED);
        overdue.setQrVerified(false);
        overdue.setConfirmedPickupTime(Instant.now().minus(30, ChronoUnit.MINUTES));

        Donation don = new Donation();
        don.setId("don-1");
        don.setDonorId("donor-1");
        don.setFoodName("Fresh Biryani");
        don.setStatus(DonationStatus.CLAIMED);
        don.setPickupDeadline(Instant.now().plus(2, ChronoUnit.HOURS));

        when(claimRepository.findByStatusAndConfirmedPickupTimeBefore(eq(ClaimStatus.APPROVED), any(Instant.class)))
                .thenReturn(List.of(overdue));
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(don));
        when(relayEventRepository.findByClaimId("clm-overdue")).thenReturn(Optional.empty());
        when(relayEventRepository.save(any(RescueRelayEvent.class))).thenAnswer(i -> {
            RescueRelayEvent e = i.getArgument(0);
            e.setId("relay-1");
            return e;
        });

        List<RescueRelayResponse> detected = rescueRelayService.detectMissedPickups(15);

        assertEquals(1, detected.size());
        assertEquals(RescueRelayStatus.AWAITING_DONOR_APPROVAL, detected.get(0).getStatus());
        assertEquals(RescueRelayStatus.AWAITING_DONOR_APPROVAL, overdue.getRescueRelayStatus());
        verify(notificationService, times(1)).sendNotification(eq("rec-late"), anyString(), anyString(), any(), anyString());
        verify(notificationService, times(1)).sendNotification(eq("donor-1"), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Donor approval releases overdue allocation, revokes passport, restores quantity, and finds alternative NGOs")
    void testApproveReleaseAndRematch() {
        Claim missedClaim = new Claim();
        missedClaim.setId("clm-missed");
        missedClaim.setDonationId("don-1");
        missedClaim.setDonorId("donor-1");
        missedClaim.setReceiverId("rec-late");
        missedClaim.setReceiverName("Late Shelter");
        missedClaim.setClaimedQuantity(20.0);
        missedClaim.setMeasurementUnit("kg");
        missedClaim.setStatus(ClaimStatus.APPROVED);
        missedClaim.setRescueRelayStatus(RescueRelayStatus.AWAITING_DONOR_APPROVAL);

        Donation don = new Donation();
        don.setId("don-1");
        don.setDonorId("donor-1");
        don.setFoodName("Fresh Biryani");
        don.setQuantity(25.0);
        don.setRemainingQuantity(5.0);
        don.setAllocatedQuantity(20.0);
        don.setAllocationCount(1);
        don.setPickupAddress("Brigade Rd");
        don.setPickupDeadline(Instant.now().plus(2, ChronoUnit.HOURS));

        Donation restoredDon = new Donation();
        restoredDon.setId("don-1");
        restoredDon.setDonorId("donor-1");
        restoredDon.setFoodName("Fresh Biryani");
        restoredDon.setQuantity(25.0);
        restoredDon.setRemainingQuantity(25.0);
        restoredDon.setAllocatedQuantity(0.0);
        restoredDon.setAllocationCount(1);
        restoredDon.setPickupAddress("Brigade Rd");
        restoredDon.setPickupDeadline(don.getPickupDeadline());

        MatchRecommendationResponse altNgo = new MatchRecommendationResponse();
        altNgo.setReceiverId("rec-backup");
        altNgo.setReceiverName("Backup Community Kitchen");
        altNgo.setDistanceKm(2.1);
        altNgo.setMatchScore(94.0);

        User donor = new User();
        donor.setId("donor-1");
        donor.setRole(Role.ROLE_DONOR);

        when(claimRepository.findById("clm-missed")).thenReturn(Optional.of(missedClaim));
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(don));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Donation.class)))
                .thenReturn(restoredDon);
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> i.getArgument(0));
        when(relayEventRepository.findByDonationId("don-1")).thenReturn(Collections.emptyList());
        when(matchingService.findMatchesForDonation(eq("don-1"), eq(Set.of("rec-late"))))
                .thenReturn(List.of(altNgo));
        when(volunteerProfileRepository.findByAvailableForDispatchTrue()).thenReturn(Collections.emptyList());
        when(relayEventRepository.findByClaimId("clm-missed")).thenReturn(Optional.empty());
        when(relayEventRepository.save(any(RescueRelayEvent.class))).thenAnswer(i -> i.getArgument(0));

        RescueRelayActionRequest req = new RescueRelayActionRequest();
        req.setExtendDeadlineMinutes(30);
        req.setNotes("Releasing to nearby kitchen");

        RescueRelayResponse result = rescueRelayService.approveReleaseAndRematch("clm-missed", req, donor);

        assertEquals(RescueRelayStatus.RELEASED, result.getStatus());
        assertEquals(ClaimStatus.RELEASED, missedClaim.getStatus());
        assertEquals(1, result.getAlternativeReceivers().size());
        assertEquals("rec-backup", result.getAlternativeReceivers().get(0).getReceiverId());
        verify(passportService, times(1)).revokePassportForClaim(eq("clm-missed"), anyString());
        verify(notificationService, times(1)).sendNotification(eq("rec-backup"), anyString(), anyString(), any(), eq("don-1"));
    }

    @Test
    @DisplayName("Unauthorized user cannot approve releasing another donor's allocation")
    void testNonOwnerCannotRelease() {
        Claim missed = new Claim();
        missed.setId("clm-1");
        missed.setDonorId("donor-1");
        missed.setStatus(ClaimStatus.APPROVED);

        User outsider = new User();
        outsider.setId("stranger-99");
        outsider.setRole(Role.ROLE_DONOR);

        when(claimRepository.findById("clm-1")).thenReturn(Optional.of(missed));

        assertThrows(ForbiddenException.class, () -> rescueRelayService.approveReleaseAndRematch("clm-1", null, outsider));
    }
}

