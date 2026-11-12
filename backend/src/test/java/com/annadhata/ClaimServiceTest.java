package com.annadhata;

import com.annadhata.dto.ClaimResponse;
import com.annadhata.dto.CreateClaimRequest;
import com.annadhata.dto.FoodPassportResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import com.annadhata.service.ClaimService;
import com.annadhata.service.ImpactService;
import com.annadhata.service.NotificationService;
import com.annadhata.service.PassportService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VolunteerProfileRepository volunteerProfileRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ImpactService impactService;

    @Mock
    private PassportService passportService;

    @Mock
    private TrustScoreService trustScoreService;

    @Mock
    private MongoTemplate mongoTemplate;

    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(
                claimRepository,
                donationRepository,
                userRepository,
                volunteerProfileRepository,
                notificationService,
                impactService,
                passportService,
                trustScoreService,
                mongoTemplate
        );
    }

    @Test
    @DisplayName("Donor cannot claim their own food donation")
    void testDonorCannotClaimOwnDonation() {
        User user = new User();
        user.setId("donor-123");
        user.setRole(Role.ROLE_DONOR);

        Donation donation = new Donation();
        donation.setId("don-1");
        donation.setDonorId("donor-123"); // same user
        donation.setStatus(DonationStatus.AVAILABLE);

        when(donationRepository.findById("don-1")).thenReturn(Optional.of(donation));

        CreateClaimRequest req = new CreateClaimRequest();
        req.setDonationId("don-1");
        req.setClaimedQuantity(10.0);

        assertThrows(BadRequestException.class, () -> claimService.createClaim(req, user));
    }

    @Test
    @DisplayName("Receiver cannot claim an expired donation")
    void testCannotClaimExpiredDonation() {
        User receiver = new User();
        receiver.setId("receiver-1");
        receiver.setRole(Role.ROLE_RECEIVER);
        receiver.setVerificationStatus(VerificationStatus.VERIFIED);

        Donation donation = new Donation();
        donation.setId("don-expired");
        donation.setDonorId("donor-999");
        donation.setStatus(DonationStatus.AVAILABLE);
        donation.setPickupDeadline(Instant.now().minus(1, ChronoUnit.HOURS)); // past deadline

        when(donationRepository.findById("don-expired")).thenReturn(Optional.of(donation));

        CreateClaimRequest req = new CreateClaimRequest();
        req.setDonationId("don-expired");
        req.setClaimedQuantity(5.0);

        assertThrows(BadRequestException.class, () -> claimService.createClaim(req, receiver));
    }

    @Test
    @DisplayName("Unverified NGO receiver is blocked from submitting claims")
    void testUnverifiedReceiverCannotClaim() {
        User unverifiedReceiver = new User();
        unverifiedReceiver.setId("receiver-pending");
        unverifiedReceiver.setRole(Role.ROLE_RECEIVER);
        unverifiedReceiver.setVerificationStatus(VerificationStatus.PENDING);

        Donation donation = new Donation();
        donation.setId("don-valid");
        donation.setDonorId("donor-999");
        donation.setQuantity(25.0);
        donation.setStatus(DonationStatus.AVAILABLE);
        donation.setPickupDeadline(Instant.now().plus(4, ChronoUnit.HOURS));

        when(donationRepository.findById("don-valid")).thenReturn(Optional.of(donation));

        CreateClaimRequest req = new CreateClaimRequest();
        req.setDonationId("don-valid");
        req.setClaimedQuantity(10.0);

        assertThrows(ForbiddenException.class, () -> claimService.createClaim(req, unverifiedReceiver));
    }

    @Test
    @DisplayName("Valid claim request persists claim with PENDING status and sends notification")
    void testValidClaimSubmission() {
        User receiver = new User();
        receiver.setId("receiver-1");
        receiver.setName("Shelter Leader");
        receiver.setOrganizationName("Shelter NGO");
        receiver.setRole(Role.ROLE_RECEIVER);
        receiver.setVerificationStatus(VerificationStatus.VERIFIED);

        Donation donation = new Donation();
        donation.setId("don-valid");
        donation.setDonorId("donor-999");
        donation.setFoodName("25 kg Biryani");
        donation.setQuantity(25.0);
        donation.setMeasurementUnit("kg");
        donation.setStatus(DonationStatus.AVAILABLE);
        donation.setPickupDeadline(Instant.now().plus(4, ChronoUnit.HOURS));

        when(donationRepository.findById("don-valid")).thenReturn(Optional.of(donation));
        when(claimRepository.findByDonationIdAndReceiverId("don-valid", "receiver-1")).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> {
            Claim c = i.getArgument(0);
            c.setId("claim-101");
            return c;
        });

        CreateClaimRequest req = new CreateClaimRequest();
        req.setDonationId("don-valid");
        req.setClaimedQuantity(25.0);
        req.setProposedPickupTime(Instant.now().plus(2, ChronoUnit.HOURS));

        ClaimResponse resp = claimService.createClaim(req, receiver);

        assertNotNull(resp);
        assertEquals(ClaimStatus.PENDING, resp.getStatus());
        assertEquals(25.0, resp.getClaimedQuantity());
        verify(notificationService, times(1)).sendNotification(eq("donor-999"), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Cannot mark a PENDING claim directly completed (prevents double impact or workflow bypass)")
    void testCannotCompletePendingClaim() {
        User receiver = new User();
        receiver.setId("receiver-1");
        receiver.setRole(Role.ROLE_RECEIVER);

        Claim pendingClaim = new Claim();
        pendingClaim.setId("c-pending");
        pendingClaim.setDonationId("don-1");
        pendingClaim.setDonorId("donor-1");
        pendingClaim.setReceiverId("receiver-1");
        pendingClaim.setStatus(ClaimStatus.PENDING);

        Donation donation = new Donation();
        donation.setId("don-1");

        when(claimRepository.findById("c-pending")).thenReturn(Optional.of(pendingClaim));
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(donation));

        assertThrows(BadRequestException.class, () -> claimService.markCompleted("c-pending", receiver));
        verify(impactService, never()).recordImpact(any(), any());
    }

    @Test
    @DisplayName("Partial Food Distribution: Multiple NGOs can claim portions within remaining quantity")
    void testPartialClaimWithinRemainingQuantity() {
        User ngo2 = new User();
        ngo2.setId("ngo-2");
        ngo2.setName("Charity Two");
        ngo2.setRole(Role.ROLE_RECEIVER);
        ngo2.setVerificationStatus(VerificationStatus.VERIFIED);

        Donation partialDonation = new Donation();
        partialDonation.setId("don-partial");
        partialDonation.setDonorId("donor-1");
        partialDonation.setFoodName("40 kg Pulao");
        partialDonation.setQuantity(40.0);
        partialDonation.setRemainingQuantity(15.0);
        partialDonation.setAllocatedQuantity(25.0);
        partialDonation.setStatus(DonationStatus.PARTIALLY_CLAIMED);
        partialDonation.setMeasurementUnit("kg");
        partialDonation.setPickupDeadline(Instant.now().plus(3, ChronoUnit.HOURS));

        when(donationRepository.findById("don-partial")).thenReturn(Optional.of(partialDonation));
        when(claimRepository.findByDonationIdAndReceiverId("don-partial", "ngo-2")).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> {
            Claim c = i.getArgument(0);
            c.setId("claim-partial-2");
            return c;
        });

        CreateClaimRequest req = new CreateClaimRequest();
        req.setDonationId("don-partial");
        req.setClaimedQuantity(10.0);
        req.setProposedPickupTime(Instant.now().plus(1, ChronoUnit.HOURS));

        ClaimResponse resp = claimService.createClaim(req, ngo2);
        assertNotNull(resp);
        assertEquals(10.0, resp.getClaimedQuantity());
        assertEquals(ClaimStatus.PENDING, resp.getStatus());

        // Attempting to claim more than 15.0 remaining must fail
        CreateClaimRequest overReq = new CreateClaimRequest();
        overReq.setDonationId("don-partial");
        overReq.setClaimedQuantity(18.0);
        assertThrows(BadRequestException.class, () -> claimService.createClaim(overReq, ngo2));
    }

    @Test
    @DisplayName("Partial Food Distribution: Approving partial claim marks PARTIALLY_CLAIMED and issues QR Food Passport")
    void testApprovePartialClaimGeneratesPassport() {
        User donor = new User();
        donor.setId("donor-1");
        donor.setRole(Role.ROLE_DONOR);

        Claim pendingClaim = new Claim();
        pendingClaim.setId("claim-p1");
        pendingClaim.setDonationId("don-40");
        pendingClaim.setDonorId("donor-1");
        pendingClaim.setReceiverId("ngo-1");
        pendingClaim.setReceiverName("Hope NGO");
        pendingClaim.setClaimedQuantity(15.0);
        pendingClaim.setMeasurementUnit("kg");
        pendingClaim.setStatus(ClaimStatus.PENDING);

        Donation updatedAfterReserve = new Donation();
        updatedAfterReserve.setId("don-40");
        updatedAfterReserve.setDonorId("donor-1");
        updatedAfterReserve.setFoodName("Pulao");
        updatedAfterReserve.setQuantity(40.0);
        updatedAfterReserve.setRemainingQuantity(25.0);
        updatedAfterReserve.setAllocatedQuantity(15.0);
        updatedAfterReserve.setAllocationCount(1);
        updatedAfterReserve.setMeasurementUnit("kg");

        FoodPassportResponse passportResp = new FoodPassportResponse();
        passportResp.setId("pass-1");
        passportResp.setPassportCode("AD-1111-2222-3333");

        when(claimRepository.findById("claim-p1")).thenReturn(Optional.of(pendingClaim));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Donation.class)))
                .thenReturn(updatedAfterReserve);
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.findByDonationId("don-40")).thenReturn(Collections.singletonList(pendingClaim));
        when(passportService.generatePassportForClaim(any(), any())).thenReturn(passportResp);

        ClaimResponse approved = claimService.approveClaim("claim-p1", donor);

        assertEquals(ClaimStatus.APPROVED, approved.getStatus());
        assertEquals("AD-1111-2222-3333", approved.getPassportCode());
        assertEquals(DonationStatus.PARTIALLY_CLAIMED, updatedAfterReserve.getStatus());
        verify(passportService, times(1)).generatePassportForClaim(any(), any());
    }
}
