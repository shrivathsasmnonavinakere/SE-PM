package com.annadhata;

import com.annadhata.dto.FoodPassportResponse;
import com.annadhata.dto.VerifyPassportRequest;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.PassportStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.FoodPassport;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.FoodPassportRepository;
import com.annadhata.service.NotificationService;
import com.annadhata.service.PassportService;
import com.annadhata.service.TrustScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassportServiceTest {

    @Mock
    private FoodPassportRepository passportRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TrustScoreService trustScoreService;

    private PassportService passportService;

    @BeforeEach
    void setUp() {
        passportService = new PassportService(
                passportRepository, claimRepository, donationRepository, notificationService, trustScoreService
        );
    }

    @Test
    @DisplayName("Generate QR Food Passport produces signed token and real PNG Base64 QR code")
    void testGeneratePassportCreatesQrPngAndValidHmac() {
        Claim claim = new Claim();
        claim.setId("clm-1");
        claim.setDonationId("don-1");
        claim.setReceiverId("rec-1");
        claim.setReceiverName("Hope NGO");
        claim.setClaimedQuantity(15.0);
        claim.setMeasurementUnit("kg");

        Donation donation = new Donation();
        donation.setId("don-1");
        donation.setDonorId("donor-1");
        donation.setDonorName("Spice Garden");
        donation.setFoodName("Vegetable Biryani");
        donation.setSafetyIndicator(SafetyIndicator.LOW_RISK);
        donation.setPickupDeadline(Instant.now().plus(3, ChronoUnit.HOURS));

        when(passportRepository.findByClaimId("clm-1")).thenReturn(Optional.empty());
        when(passportRepository.save(any(FoodPassport.class))).thenAnswer(i -> {
            FoodPassport p = i.getArgument(0);
            p.setId("pass-100");
            return p;
        });
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        FoodPassportResponse resp = passportService.generatePassportForClaim(claim, donation);

        assertNotNull(resp);
        assertTrue(resp.getPassportCode().startsWith("AD-"));
        assertEquals(PassportStatus.ACTIVE, resp.getStatus());
        assertNotNull(resp.getHmacSignature());
        assertTrue(resp.getQrImageBase64().startsWith("data:image/png;base64,"));
        assertTrue(resp.getQrPayloadJson().contains(resp.getPassportCode()));
    }

    @Test
    @DisplayName("Verify QR Food Passport transitions claim to PICKED_UP when scanned by owning donor")
    void testVerifyPassportTransitionsClaimToPickedUp() {
        String code = "AD-ABCD-1234-5678";
        String expectedHmac = passportService.computeHmac("clm-1", "don-1", "rec-1", 15.0, code);

        FoodPassport passport = new FoodPassport();
        passport.setId("pass-1");
        passport.setClaimId("clm-1");
        passport.setDonationId("don-1");
        passport.setDonorId("donor-1");
        passport.setReceiverId("rec-1");
        passport.setFoodName("Biryani");
        passport.setAllocatedQuantity(15.0);
        passport.setMeasurementUnit("kg");
        passport.setPassportCode(code);
        passport.setHmacSignature(expectedHmac);
        passport.setStatus(PassportStatus.ACTIVE);
        passport.setExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));

        Claim claim = new Claim();
        claim.setId("clm-1");
        claim.setDonationId("don-1");
        claim.setDonorId("donor-1");
        claim.setReceiverId("rec-1");
        claim.setStatus(ClaimStatus.APPROVED);

        Donation donation = new Donation();
        donation.setId("don-1");
        donation.setRemainingQuantity(0.0);

        User donor = new User();
        donor.setId("donor-1");
        donor.setName("Spice Garden");
        donor.setRole(Role.ROLE_DONOR);

        when(passportRepository.findByPassportCode(code)).thenReturn(Optional.of(passport));
        when(claimRepository.findById("clm-1")).thenReturn(Optional.of(claim));
        when(passportRepository.save(any(FoodPassport.class))).thenAnswer(i -> i.getArgument(0));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(donation));

        VerifyPassportRequest req = new VerifyPassportRequest();
        req.setPassportCode(code);

        FoodPassportResponse verified = passportService.verifyPassport(req, donor);

        assertEquals(PassportStatus.VERIFIED_PICKUP, verified.getStatus());
        assertEquals(ClaimStatus.PICKED_UP, claim.getStatus());
        assertTrue(claim.isQrVerified());
    }

    @Test
    @DisplayName("Receiver or stranger cannot self-verify passport, and tampered HMAC signature is rejected")
    void testVerifyPassportSecurityChecks() {
        String code = "AD-ABCD-1234-5678";
        String expectedHmac = passportService.computeHmac("clm-1", "don-1", "rec-1", 15.0, code);

        FoodPassport passport = new FoodPassport();
        passport.setId("pass-1");
        passport.setClaimId("clm-1");
        passport.setDonationId("don-1");
        passport.setDonorId("donor-1");
        passport.setReceiverId("rec-1");
        passport.setAllocatedQuantity(15.0);
        passport.setPassportCode(code);
        passport.setHmacSignature(expectedHmac);
        passport.setStatus(PassportStatus.ACTIVE);
        passport.setExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));

        Claim claim = new Claim();
        claim.setId("clm-1");
        claim.setStatus(ClaimStatus.APPROVED);

        User receiver = new User();
        receiver.setId("rec-1");
        receiver.setRole(Role.ROLE_RECEIVER);

        when(passportRepository.findByPassportCode(code)).thenReturn(Optional.of(passport));
        when(claimRepository.findById("clm-1")).thenReturn(Optional.of(claim));

        VerifyPassportRequest req = new VerifyPassportRequest();
        req.setPassportCode(code);
        assertThrows(ForbiddenException.class, () -> passportService.verifyPassport(req, receiver));

        // Donor scanning tampered JSON signature must be rejected
        User donor = new User();
        donor.setId("donor-1");
        donor.setRole(Role.ROLE_DONOR);

        VerifyPassportRequest tamperedReq = new VerifyPassportRequest();
        tamperedReq.setPassportCode("{\"passportCode\":\"" + code + "\",\"sig\":\"deadbeef00000000\"}");
        assertThrows(ForbiddenException.class, () -> passportService.verifyPassport(tamperedReq, donor));

        // Revoked passport cannot verify
        passport.setStatus(PassportStatus.REVOKED);
        VerifyPassportRequest cleanReq = new VerifyPassportRequest();
        cleanReq.setPassportCode(code);
        assertThrows(BadRequestException.class, () -> passportService.verifyPassport(cleanReq, donor));
    }
}

