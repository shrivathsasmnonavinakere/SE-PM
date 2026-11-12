package com.annadhata;

import com.annadhata.dto.VolunteerTaskResponse;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VolunteerProfileRepository;
import com.annadhata.service.ImpactService;
import com.annadhata.service.NotificationService;
import com.annadhata.service.TrustScoreService;
import com.annadhata.service.VolunteerService;
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

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceTest {

    @Mock
    private VolunteerProfileRepository volunteerProfileRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ImpactService impactService;
    @Mock
    private TrustScoreService trustScoreService;
    @Mock
    private MongoTemplate mongoTemplate;

    private VolunteerService volunteerService;

    @BeforeEach
    void setUp() {
        volunteerService = new VolunteerService(
                volunteerProfileRepository, claimRepository, donationRepository, userRepository,
                notificationService, impactService, trustScoreService, mongoTemplate
        );
    }

    @Test
    @DisplayName("Volunteer can atomically accept open mission and receive donor & receiver dispatch notification")
    void testAcceptTaskAtomically() {
        User vol = new User("Karthik", "vol@annadhata.org", "pw", Role.ROLE_VOLUNTEER);
        vol.setId("vol-1");
        vol.setPhone("9900112233");
        vol.setActive(true);

        VolunteerProfile vp = new VolunteerProfile();
        vp.setUserId("vol-1");
        vp.setVehicleType("Scooter");

        Claim assigned = new Claim();
        assigned.setId("clm-1");
        assigned.setDonationId("don-1");
        assigned.setDonorId("donor-1");
        assigned.setReceiverId("rec-1");
        assigned.setReceiverName("Hope NGO");
        assigned.setClaimedQuantity(15.0);
        assigned.setMeasurementUnit("kg");
        assigned.setStatus(ClaimStatus.APPROVED);
        assigned.setAssignedVolunteerId("vol-1");
        assigned.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.ASSIGNED);

        Donation don = new Donation();
        don.setId("don-1");
        don.setFoodName("Biryani");

        when(volunteerProfileRepository.findByUserId("vol-1")).thenReturn(Optional.of(vp));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Claim.class)))
                .thenReturn(assigned);
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(don));
        when(userRepository.findById("rec-1")).thenReturn(Optional.empty());

        VolunteerTaskResponse res = volunteerService.acceptTask("clm-1", vol);

        assertEquals("vol-1", res.getAssignedVolunteerId());
        assertEquals(VolunteerAssignmentStatus.ASSIGNED, res.getVolunteerAssignmentStatus());
        verify(notificationService, times(1)).sendNotification(eq("donor-1"), anyString(), anyString(), any(), eq("clm-1"));
        verify(notificationService, times(1)).sendNotification(eq("rec-1"), anyString(), anyString(), any(), eq("clm-1"));
    }

    @Test
    @DisplayName("Concurrent acceptance of already claimed volunteer mission fails cleanly")
    void testAcceptAlreadyAssignedMissionRejected() {
        User vol = new User("Karthik", "vol@annadhata.org", "pw", Role.ROLE_VOLUNTEER);
        vol.setId("vol-2");
        vol.setActive(true);

        when(volunteerProfileRepository.findByUserId("vol-2")).thenReturn(Optional.of(new VolunteerProfile()));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Claim.class)))
                .thenReturn(null);

        assertThrows(BadRequestException.class, () -> volunteerService.acceptTask("clm-taken", vol));
    }

    @Test
    @DisplayName("Marking volunteer delivery complete records impact and updates completed transport stats")
    void testMarkDeliveredUpdatesStatsAndImpact() {
        User vol = new User("Karthik", "vol@annadhata.org", "pw", Role.ROLE_VOLUNTEER);
        vol.setId("vol-1");
        vol.setRole(Role.ROLE_VOLUNTEER);
        vol.setActive(true);

        Claim claim = new Claim();
        claim.setId("clm-1");
        claim.setDonationId("don-1");
        claim.setDonorId("donor-1");
        claim.setReceiverId("rec-1");
        claim.setReceiverName("Hope NGO");
        claim.setClaimedQuantity(20.0);
        claim.setMeasurementUnit("kg");
        claim.setAssignedVolunteerId("vol-1");
        claim.setStatus(ClaimStatus.PICKED_UP);

        Donation don = new Donation();
        don.setId("don-1");
        don.setFoodName("Rice & Dal");
        don.setRemainingQuantity(0.0);
        don.setStatus(DonationStatus.PICKED_UP);

        VolunteerProfile vp = new VolunteerProfile();
        vp.setUserId("vol-1");
        vp.setCompletedDeliveriesCount(1);
        vp.setTotalKgTransported(10.0);

        when(claimRepository.findById("clm-1")).thenReturn(Optional.of(claim));
        when(donationRepository.findById("don-1")).thenReturn(Optional.of(don));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        when(volunteerProfileRepository.findByUserId("vol-1")).thenReturn(Optional.of(vp));
        when(claimRepository.findByDonationId("don-1")).thenReturn(Collections.singletonList(claim));
        when(userRepository.findById("rec-1")).thenReturn(Optional.empty());

        VolunteerTaskResponse res = volunteerService.markDelivered("clm-1", vol);

        assertEquals(ClaimStatus.COMPLETED, res.getClaimStatus());
        assertEquals(VolunteerAssignmentStatus.DELIVERED, res.getVolunteerAssignmentStatus());
        assertEquals(2, vp.getCompletedDeliveriesCount());
        assertEquals(30.0, vp.getTotalKgTransported());
        verify(impactService, times(1)).recordImpact(don, claim);
    }
}

