package com.annadhata;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.AiExtractionResponse;
import com.annadhata.dto.AiSafetyEvaluationResponse;
import com.annadhata.dto.AuthResponse;
import com.annadhata.dto.RegisterRequest;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.model.Donation;
import com.annadhata.model.Notification;
import com.annadhata.model.User;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.repository.NotificationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VerificationRequestRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import com.annadhata.security.JwtService;
import com.annadhata.service.AuthService;
import com.annadhata.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAndAiUpgradeTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationRequestRepository verificationRequestRepository;
    @Mock
    private VolunteerProfileRepository volunteerProfileRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Self-registration blocks ROLE_ADMIN privilege escalation")
    void testSelfRegisterRejectsAdminRole() {
        AuthService authService = new AuthService(
                userRepository, verificationRequestRepository, volunteerProfileRepository,
                new BCryptPasswordEncoder(), new JwtService(), authenticationManager
        );

        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setName("Evil Escalator");
        adminReq.setEmail("hacker@test.org");
        adminReq.setPassword("Password@123");
        adminReq.setRole(Role.ROLE_ADMIN);

        assertThrows(ForbiddenException.class, () -> authService.register(adminReq));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Self-registration with ROLE_VOLUNTEER creates VolunteerProfile automatically")
    void testRegisterVolunteerCreatesProfile() {
        JwtService jwtService = new JwtService();
        // Set private jwt secret field cleanly via reflection for isolated unit testing without Spring context
        org.springframework.test.util.ReflectionTestUtils.setField(jwtService, "secretKey", "AnnaDhataUnitTestSecretKeyWhichIsLongEnoughForHmac512Validation1234567890");
        org.springframework.test.util.ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        AuthService authService = new AuthService(
                userRepository, verificationRequestRepository, volunteerProfileRepository,
                new BCryptPasswordEncoder(), jwtService, authenticationManager
        );

        RegisterRequest volReq = new RegisterRequest();
        volReq.setName("Volunteer Priya");
        volReq.setEmail("priya@volunteer.org");
        volReq.setPassword("Password@123");
        volReq.setRole(Role.ROLE_VOLUNTEER);
        volReq.setVehicleType("Car / Hatchback");
        volReq.setMaxCapacityKg(50.0);

        when(userRepository.existsByEmail("priya@volunteer.org")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("vol-123");
            return u;
        });

        AuthResponse resp = authService.register(volReq);

        assertEquals(Role.ROLE_VOLUNTEER, resp.getRole());
        verify(volunteerProfileRepository, times(1)).save(any(VolunteerProfile.class));
    }

    @Test
    @DisplayName("IDOR check prevents marking another user's notification as read")
    void testNotificationMarkAsReadBlocksOtherUsers() {
        NotificationService notificationService = new NotificationService(notificationRepository);

        Notification victimNotif = new Notification();
        victimNotif.setId("notif-1");
        victimNotif.setRecipientUserId("victim-user");

        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(victimNotif));

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead("notif-1", "attacker-user"));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("AI Food Safety Screening enforces deterministic spoilage keyword escalation (HIGH_RISK)")
    void testDeterministicSafetyFlagsSpoilageRedFlags() {
        GeminiAIService aiService = new GeminiAIService();

        Donation risky = new Donation();
        risky.setFoodName("Cooked Pasta");
        risky.setFoodCategory(FoodCategory.COOKED_FOOD);
        risky.setFoodDescription("Left unrefrigerated overnight with sour smell");
        risky.setPreparationDateTime(Instant.now().minus(14, ChronoUnit.HOURS));
        risky.setPickupDeadline(Instant.now().plus(2, ChronoUnit.HOURS));

        AiSafetyEvaluationResponse resp = aiService.evaluateFoodSafety(risky);

        assertEquals(SafetyIndicator.HIGH_RISK, resp.getSafetyIndicator());
        assertTrue(resp.isRequiresAdminReview());
        assertFalse(resp.getIdentifiedConcerns().isEmpty());
    }

    @Test
    @DisplayName("NLP Donation Assistant parses explicit evening pickup deadlines accurately")
    void testNlpExtractsTimesCleanly() {
        GeminiAIService aiService = new GeminiAIService();
        AiExtractionResponse extracted = aiService.extractDonationDetails(
                "We have 32.5 kg of Chicken Biryani prepared at 6 PM, must be collected before 9:30 PM."
        );

        assertEquals(32.5, extracted.getQuantity());
        assertEquals("kg", extracted.getUnit());
        assertEquals(FoodCategory.COOKED_FOOD, extracted.getFoodCategory());
        assertTrue(extracted.getPickupDeadline().isAfter(extracted.getPreparationDateTime()));
    }
}

