package com.annadhata;

import com.annadhata.dto.TrustScoreResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.TrustScore;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.TrustScoreRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.service.TrustScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrustScoreServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private TrustScoreRepository trustScoreRepository;

    private TrustScoreService trustScoreService;

    @BeforeEach
    void setUp() {
        trustScoreService = new TrustScoreService(
                userRepository, donationRepository, claimRepository, trustScoreRepository
        );
    }

    @Test
    @DisplayName("Receiver trust score reflects QR-verified on-time pickups vs missed pickup penalties transparently")
    void testReceiverTrustScoreBreakdown() {
        User reliableNgo = new User("Hope Care", "hope@ngo.org", "pw", Role.ROLE_RECEIVER);
        reliableNgo.setId("ngo-good");
        reliableNgo.setVerificationStatus(VerificationStatus.VERIFIED);
        reliableNgo.setActive(true);

        Claim c1 = new Claim();
        c1.setStatus(ClaimStatus.COMPLETED);
        c1.setQrVerified(true);

        Claim c2 = new Claim();
        c2.setStatus(ClaimStatus.COMPLETED);
        c2.setQrVerified(true);

        when(userRepository.findById("ngo-good")).thenReturn(Optional.of(reliableNgo));
        when(trustScoreRepository.findByUserId("ngo-good")).thenReturn(Optional.empty());
        when(claimRepository.findByReceiverIdOrderByCreatedAtDesc("ngo-good")).thenReturn(List.of(c1, c2));
        when(trustScoreRepository.save(any(TrustScore.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        TrustScoreResponse goodScore = trustScoreService.recalculateAndSave("ngo-good");

        // Now test an NGO with missed pickups
        User flakyNgo = new User("Late NGO", "late@ngo.org", "pw", Role.ROLE_RECEIVER);
        flakyNgo.setId("ngo-flaky");
        flakyNgo.setVerificationStatus(VerificationStatus.VERIFIED);
        flakyNgo.setActive(true);

        Claim missed1 = new Claim();
        missed1.setStatus(ClaimStatus.RELEASED);
        missed1.setRescueRelayStatus(RescueRelayStatus.RELEASED);
        Claim missed2 = new Claim();
        missed2.setStatus(ClaimStatus.RELEASED);
        missed2.setRescueRelayStatus(RescueRelayStatus.RELEASED);

        when(userRepository.findById("ngo-flaky")).thenReturn(Optional.of(flakyNgo));
        when(trustScoreRepository.findByUserId("ngo-flaky")).thenReturn(Optional.empty());
        when(claimRepository.findByReceiverIdOrderByCreatedAtDesc("ngo-flaky")).thenReturn(List.of(missed1, missed2));

        TrustScoreResponse flakyScore = trustScoreService.recalculateAndSave("ngo-flaky");

        assertTrue(goodScore.getOverallScore() >= 90.0);
        assertEquals("PLATINUM", goodScore.getTier());
        assertEquals(4, goodScore.getFactors().size());
        assertTrue(goodScore.getOverallScore() > flakyScore.getOverallScore());
        assertEquals(2, flakyScore.getMissedPickups());
    }

    @Test
    @DisplayName("Donor trust score weighs completed donations vs safety flags transparently")
    void testDonorTrustScoreCalculation() {
        User donor = new User("Kitchen", "k@r.com", "pw", Role.ROLE_DONOR);
        donor.setId("don-user-1");
        donor.setVerificationStatus(VerificationStatus.VERIFIED);
        donor.setActive(true);

        Donation d1 = new Donation();
        d1.setStatus(DonationStatus.COMPLETED);
        d1.setSafetyIndicator(SafetyIndicator.LOW_RISK);

        Donation d2 = new Donation();
        d2.setStatus(DonationStatus.COMPLETED);
        d2.setSafetyIndicator(SafetyIndicator.LOW_RISK);

        when(userRepository.findById("don-user-1")).thenReturn(Optional.of(donor));
        when(trustScoreRepository.findByUserId("don-user-1")).thenReturn(Optional.empty());
        when(donationRepository.findByDonorId("don-user-1")).thenReturn(List.of(d1, d2));
        when(claimRepository.findByDonorIdOrderByCreatedAtDesc("don-user-1")).thenReturn(List.of());
        when(trustScoreRepository.save(any(TrustScore.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        TrustScoreResponse resp = trustScoreService.recalculateAndSave("don-user-1");

        assertEquals(2, resp.getCompletedCount());
        assertTrue(resp.getOverallScore() > 85.0);
        assertEquals(4, resp.getFactors().size());
    }
}

