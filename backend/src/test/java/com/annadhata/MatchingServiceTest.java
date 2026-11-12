package com.annadhata;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.matching.MatchingService;
import com.annadhata.model.Donation;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.model.User;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.MatchResultRepository;
import com.annadhata.repository.ReceiverNeedRepository;
import com.annadhata.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private ReceiverNeedRepository receiverNeedRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiAIService geminiAIService;

    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(
                donationRepository,
                receiverNeedRepository,
                matchResultRepository,
                userRepository,
                geminiAIService
        );
    }

    @Test
    @DisplayName("Matching engine correctly ranks nearest receiver with matching category highest")
    void testMatchingEngineRanking() {
        Donation donation = new Donation();
        donation.setId("don-1");
        donation.setFoodName("25 kg Biryani");
        donation.setFoodCategory(FoodCategory.COOKED_FOOD);
        donation.setQuantity(25.0);
        donation.setMeasurementUnit("kg");
        donation.setLatitude(12.9716);
        donation.setLongitude(77.5946);
        donation.setStatus(DonationStatus.AVAILABLE);
        donation.setPickupDeadline(Instant.now().plus(3, ChronoUnit.HOURS));

        // Close NGO with same category (Cooked food) ~ 1 km away
        ReceiverNeed need1 = new ReceiverNeed();
        need1.setId("need-1");
        need1.setReceiverId("rec-1");
        need1.setReceiverName("Nearby Shelter");
        need1.setRequiredCategory(FoodCategory.COOKED_FOOD);
        need1.setRequiredQuantity(25.0);
        need1.setMaxReceivingCapacity(50.0);
        need1.setLatitude(12.9720);
        need1.setLongitude(77.5950);
        need1.setActive(true);

        // Farther NGO with different category (Vegetables) ~ 20 km away
        ReceiverNeed need2 = new ReceiverNeed();
        need2.setId("need-2");
        need2.setReceiverId("rec-2");
        need2.setReceiverName("Distant Kitchen");
        need2.setRequiredCategory(FoodCategory.VEGETABLES);
        need2.setRequiredQuantity(50.0);
        need2.setMaxReceivingCapacity(100.0);
        need2.setLatitude(12.8000);
        need2.setLongitude(77.5000);
        need2.setActive(true);

        User rec1User = new User();
        rec1User.setId("rec-1");
        rec1User.setRole(Role.ROLE_RECEIVER);
        rec1User.setVerificationStatus(VerificationStatus.VERIFIED);
        rec1User.setActive(true);

        User rec2User = new User();
        rec2User.setId("rec-2");
        rec2User.setRole(Role.ROLE_RECEIVER);
        rec2User.setVerificationStatus(VerificationStatus.VERIFIED);
        rec2User.setActive(true);

        when(donationRepository.findById("don-1")).thenReturn(Optional.of(donation));
        when(receiverNeedRepository.findByActiveTrue()).thenReturn(Arrays.asList(need1, need2));
        when(userRepository.findAllById(any())).thenReturn(Arrays.asList(rec1User, rec2User));
        when(geminiAIService.explainMatchRecommendation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn("Recommended due to high proximity and matching category.");

        List<MatchRecommendationResponse> matches = matchingService.findMatchesForDonation("don-1");

        assertEquals(2, matches.size());
        assertEquals("rec-1", matches.get(0).getReceiverId(), "Nearby shelter should be ranked 1st");
        assertTrue(matches.get(0).getMatchScore() > matches.get(1).getMatchScore(), "Score of nearby shelter should be higher");
        verify(matchResultRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Matching excludes unverified or suspended NGOs")
    void testMatchingExcludesUnverifiedOrSuspendedReceivers() {
        Donation donation = new Donation();
        donation.setId("don-1");
        donation.setDonorId("donor-1");
        donation.setFoodName("Biryani");
        donation.setFoodCategory(FoodCategory.COOKED_FOOD);
        donation.setQuantity(15.0);
        donation.setLatitude(12.9716);
        donation.setLongitude(77.5946);
        donation.setStatus(DonationStatus.AVAILABLE);
        donation.setPickupDeadline(Instant.now().plus(3, ChronoUnit.HOURS));

        ReceiverNeed pendingNeed = new ReceiverNeed();
        pendingNeed.setReceiverId("rec-unverified");
        pendingNeed.setReceiverName("Pending NGO");
        pendingNeed.setRequiredCategory(FoodCategory.COOKED_FOOD);
        pendingNeed.setLatitude(12.9716);
        pendingNeed.setLongitude(77.5946);
        pendingNeed.setActive(true);

        User unverifiedUser = new User();
        unverifiedUser.setId("rec-unverified");
        unverifiedUser.setRole(Role.ROLE_RECEIVER);
        unverifiedUser.setVerificationStatus(VerificationStatus.PENDING);
        unverifiedUser.setActive(true);

        when(donationRepository.findById("don-1")).thenReturn(Optional.of(donation));
        when(receiverNeedRepository.findByActiveTrue()).thenReturn(List.of(pendingNeed));
        when(userRepository.findAllById(any())).thenReturn(List.of(unverifiedUser));

        List<MatchRecommendationResponse> matches = matchingService.findMatchesForDonation("don-1");
        assertTrue(matches.isEmpty(), "Unverified receivers must never be recommended");
    }

    @Test
    @DisplayName("Matching returns empty list when donation is expired or completed")
    void testMatchingIgnoresExpiredDonations() {
        Donation donation = new Donation();
        donation.setId("don-expired");
        donation.setStatus(DonationStatus.EXPIRED);

        when(donationRepository.findById("don-expired")).thenReturn(Optional.of(donation));

        List<MatchRecommendationResponse> matches = matchingService.findMatchesForDonation("don-expired");
        assertTrue(matches.isEmpty());
        verifyNoInteractions(receiverNeedRepository);
    }
}
