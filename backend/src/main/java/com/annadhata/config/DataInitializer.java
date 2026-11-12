package com.annadhata.config;

import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.Role;
import com.annadhata.enums.SafetyIndicator;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.model.*;
import com.annadhata.repository.*;
import com.annadhata.service.PassportService;
import com.annadhata.service.TrustScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final ReceiverNeedRepository receiverNeedRepository;
    private final ClaimRepository claimRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PassportService passportService;
    private final TrustScoreService trustScoreService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DonationRepository donationRepository,
                           ReceiverNeedRepository receiverNeedRepository,
                           ClaimRepository claimRepository,
                           ImpactRecordRepository impactRecordRepository,
                           VolunteerProfileRepository volunteerProfileRepository,
                           PassportService passportService,
                           TrustScoreService trustScoreService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
        this.receiverNeedRepository = receiverNeedRepository;
        this.claimRepository = claimRepository;
        this.impactRecordRepository = impactRecordRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passportService = passportService;
        this.trustScoreService = trustScoreService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedInitialDataIfEmpty();
    }

    public synchronized void seedInitialDataIfEmpty() {
        if (userRepository.count() > 0) {
            log.info("Database already initialized ({} users). Running idempotent schema backfill & v2 checks...", userRepository.count());
            ensureV2UpgradeAndBackfill();
            return;
        }

        log.info("Initializing demo seed data for AnnaDhata platform...");

        // 1. Admin
        User admin = new User("AnnaDhata Admin", "admin@annadhata.org", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
        admin.setOrganizationName("AnnaDhata Platform Governance");
        admin.setPhone("+91 98765 43210");
        admin.setAddress("MG Road, Bangalore, Karnataka");
        admin.setVerificationStatus(VerificationStatus.VERIFIED);
        admin.setLatitude(12.9716);
        admin.setLongitude(77.5946);
        userRepository.save(admin);

        // 2. Donor 1: The Spice Garden Restaurant (Brigade Road)
        User donor1 = new User("Chef Rajesh Sharma", "spicegarden@restaurant.com", passwordEncoder.encode("donor123"), Role.ROLE_DONOR);
        donor1.setOrganizationName("The Spice Garden Restaurant");
        donor1.setOrganizationType("Restaurant");
        donor1.setPhone("+91 98450 11223");
        donor1.setAddress("12, Brigade Road, Ashok Nagar, Bangalore");
        donor1.setVerificationStatus(VerificationStatus.VERIFIED);
        donor1.setLatitude(12.9719);
        donor1.setLongitude(77.6070);
        User savedDonor1 = userRepository.save(donor1);

        // 3. Donor 2: Royal Palace Hotel Kitchen (Residency Road)
        User donor2 = new User("Chef Meera Rao", "royalpalace@hotel.com", passwordEncoder.encode("donor123"), Role.ROLE_DONOR);
        donor2.setOrganizationName("Royal Palace Hotel & Banquets");
        donor2.setOrganizationType("Hotel & Catering");
        donor2.setPhone("+91 98450 33445");
        donor2.setAddress("44, Residency Road, Shanthala Nagar, Bangalore");
        donor2.setVerificationStatus(VerificationStatus.VERIFIED);
        donor2.setLatitude(12.9680);
        donor2.setLongitude(77.6010);
        User savedDonor2 = userRepository.save(donor2);

        // 4. Receiver 1: Hope Care Foundation (Richmond Town)
        User receiver1 = new User("Sunita Nair", "hope@shelter.org", passwordEncoder.encode("receiver123"), Role.ROLE_RECEIVER);
        receiver1.setOrganizationName("Hope Care Foundation & Shelter");
        receiver1.setOrganizationType("Shelter & Orphanage");
        receiver1.setRegistrationNumber("NGO-BLR-2021-884");
        receiver1.setPhone("+91 98860 99887");
        receiver1.setAddress("78, Richmond Town, Bangalore");
        receiver1.setVerificationStatus(VerificationStatus.VERIFIED);
        receiver1.setVerificationDocuments(Arrays.asList("https://images.unsplash.com/photo-1557804506-669a67965ba0"));
        receiver1.setLatitude(12.9640);
        receiver1.setLongitude(77.6050);
        User savedReceiver1 = userRepository.save(receiver1);

        // 5. Receiver 2: Anna Seva Community Kitchen (Indiranagar)
        User receiver2 = new User("Anand Kumar", "annaseva@charity.org", passwordEncoder.encode("receiver123"), Role.ROLE_RECEIVER);
        receiver2.setOrganizationName("Anna Seva Community Kitchen");
        receiver2.setOrganizationType("Community Kitchen");
        receiver2.setRegistrationNumber("NGO-BLR-2019-312");
        receiver2.setPhone("+91 98860 55443");
        receiver2.setAddress("105, Indiranagar 100ft Road, Bangalore");
        receiver2.setVerificationStatus(VerificationStatus.VERIFIED);
        receiver2.setVerificationDocuments(Arrays.asList("https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca"));
        receiver2.setLatitude(12.9784);
        receiver2.setLongitude(77.6408);
        User savedReceiver2 = userRepository.save(receiver2);

        // 5b. Verified Volunteer Courier
        User volunteer = new User("Karthik Hegde", "volunteer@annadhata.org", passwordEncoder.encode("volunteer123"), Role.ROLE_VOLUNTEER);
        volunteer.setOrganizationName("AnnaDhata Rapid Volunteer Squad");
        volunteer.setOrganizationType("Volunteer Courier");
        volunteer.setPhone("+91 99001 44556");
        volunteer.setAddress("22, Koramangala 4th Block, Bangalore");
        volunteer.setVerificationStatus(VerificationStatus.VERIFIED);
        volunteer.setLatitude(12.9352);
        volunteer.setLongitude(77.6245);
        User savedVolunteer = userRepository.save(volunteer);

        VolunteerProfile vp = new VolunteerProfile();
        vp.setUserId(savedVolunteer.getId());
        vp.setUserName(savedVolunteer.getName());
        vp.setPhone(savedVolunteer.getPhone());
        vp.setVehicleType("Cargo Scooter / Van");
        vp.setMaxCapacityKg(40.0);
        vp.setServiceRadiusKm(25.0);
        vp.setAvailableForDispatch(true);
        vp.setLatitude(savedVolunteer.getLatitude());
        vp.setLongitude(savedVolunteer.getLongitude());
        vp.setAddress(savedVolunteer.getAddress());
        vp.setCompletedDeliveriesCount(2);
        vp.setTotalKgTransported(38.0);
        volunteerProfileRepository.save(vp);

        // 6. Receiver Needs across Bengaluru Zones
        ReceiverNeed need1 = new ReceiverNeed();
        need1.setReceiverId(savedReceiver1.getId());
        need1.setReceiverName(savedReceiver1.getOrganizationName());
        need1.setRequiredCategory(FoodCategory.COOKED_FOOD);
        need1.setRequiredQuantity(35.0);
        need1.setNumberOfBeneficiaries(85);
        need1.setMaxReceivingCapacity(60.0);
        need1.setPreferredFoodType("Vegetarian biryani, rice meals, roti dal");
        need1.setRequiredDate(LocalDate.now());
        need1.setPickupAvailable(true);
        need1.setPickupWindowStart("18:00");
        need1.setPickupWindowEnd("22:00");
        need1.setLatitude(savedReceiver1.getLatitude());
        need1.setLongitude(savedReceiver1.getLongitude());
        need1.setAddress(savedReceiver1.getAddress());
        need1.setActive(true);
        receiverNeedRepository.save(need1);

        ReceiverNeed need2 = new ReceiverNeed();
        need2.setReceiverId(savedReceiver2.getId());
        need2.setReceiverName(savedReceiver2.getOrganizationName());
        need2.setRequiredCategory(FoodCategory.RICE_AND_GRAINS);
        need2.setRequiredQuantity(55.0);
        need2.setNumberOfBeneficiaries(140);
        need2.setMaxReceivingCapacity(100.0);
        need2.setPreferredFoodType("Cooked rice, pulao, dry grains");
        need2.setRequiredDate(LocalDate.now());
        need2.setPickupAvailable(true);
        need2.setPickupWindowStart("12:00");
        need2.setPickupWindowEnd("21:30");
        need2.setLatitude(savedReceiver2.getLatitude());
        need2.setLongitude(savedReceiver2.getLongitude());
        need2.setAddress(savedReceiver2.getAddress());
        need2.setActive(true);
        receiverNeedRepository.save(need2);

        // 7. Food Donations & Partial Distribution Showcase
        Instant now = Instant.now();

        // Donation 1: 25 kg Vegetable Biryani (AVAILABLE)
        Donation d1 = new Donation();
        d1.setDonorId(savedDonor1.getId());
        d1.setDonorName(savedDonor1.getOrganizationName());
        d1.setDonorPhone(savedDonor1.getPhone());
        d1.setFoodName("Fresh Vegetable Biryani with Raita");
        d1.setFoodDescription("Prepared for banquet lunch. High grade basmati rice, mixed farm vegetables and aromatic spices. Packed in food-grade thermoware.");
        d1.setFoodCategory(FoodCategory.COOKED_FOOD);
        d1.setQuantity(25.0);
        d1.setRemainingQuantity(25.0);
        d1.setMeasurementUnit("kg");
        d1.setFoodImage("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop");
        d1.setPreparationDateTime(now.minus(2, ChronoUnit.HOURS));
        d1.setPickupDeadline(now.plus(4, ChronoUnit.HOURS));
        d1.setPickupAddress(savedDonor1.getAddress());
        d1.setLatitude(savedDonor1.getLatitude());
        d1.setLongitude(savedDonor1.getLongitude());
        d1.setFoodHandlingDetails("Kept in insulated hot food warmers above 65°C.");
        d1.setStorageInformation("Sealed in hygienic stainless steel food containers.");
        d1.setStatus(DonationStatus.AVAILABLE);
        d1.setSafetyIndicator(SafetyIndicator.LOW_RISK);
        d1.setSafetyNotes("Standard safety parameters verified. Fresh banquet surplus.");
        donationRepository.save(d1);

        // Donation 2: 15 kg Artisan Bakery Assortment (AVAILABLE)
        Donation d2 = new Donation();
        d2.setDonorId(savedDonor2.getId());
        d2.setDonorName(savedDonor2.getOrganizationName());
        d2.setDonorPhone(savedDonor2.getPhone());
        d2.setFoodName("Artisan Bakery Assortment");
        d2.setFoodDescription("Whole wheat bread loaves, dinner rolls, and butter croissants baked this morning.");
        d2.setFoodCategory(FoodCategory.BREAD_AND_BAKERY);
        d2.setQuantity(15.0);
        d2.setRemainingQuantity(15.0);
        d2.setMeasurementUnit("kg");
        d2.setFoodImage("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800&auto=format&fit=crop");
        d2.setPreparationDateTime(now.minus(4, ChronoUnit.HOURS));
        d2.setPickupDeadline(now.plus(8, ChronoUnit.HOURS));
        d2.setPickupAddress(savedDonor2.getAddress());
        d2.setLatitude(savedDonor2.getLatitude());
        d2.setLongitude(savedDonor2.getLongitude());
        d2.setFoodHandlingDetails("Individually paper wrapped in cardboard bakery crates.");
        d2.setStorageInformation("Ambient room temperature in dry conditions.");
        d2.setStatus(DonationStatus.AVAILABLE);
        d2.setSafetyIndicator(SafetyIndicator.LOW_RISK);
        d2.setSafetyNotes("Dry bakery goods, low microbiological risk within deadline.");
        donationRepository.save(d2);

        // Donation 3: Completed Donation with QR-verified Claim & Impact Record
        Donation d3 = new Donation();
        d3.setDonorId(savedDonor1.getId());
        d3.setDonorName(savedDonor1.getOrganizationName());
        d3.setDonorPhone(savedDonor1.getPhone());
        d3.setFoodName("Steamed Jeera Rice & Tadka Dal");
        d3.setFoodDescription("Freshly cooked lunch surplus distributed to community shelter.");
        d3.setFoodCategory(FoodCategory.COOKED_FOOD);
        d3.setQuantity(30.0);
        d3.setRemainingQuantity(0.0);
        d3.setAllocatedQuantity(30.0);
        d3.setAllocationCount(1);
        d3.setMeasurementUnit("kg");
        d3.setPreparationDateTime(now.minus(28, ChronoUnit.HOURS));
        d3.setPickupDeadline(now.minus(24, ChronoUnit.HOURS));
        d3.setPickupAddress(savedDonor1.getAddress());
        d3.setLatitude(savedDonor1.getLatitude());
        d3.setLongitude(savedDonor1.getLongitude());
        d3.setStatus(DonationStatus.COMPLETED);
        d3.setClaimedByReceiverId(savedReceiver1.getId());
        d3.setClaimedByReceiverName(savedReceiver1.getOrganizationName());
        Donation savedD3 = donationRepository.save(d3);

        Claim claim3 = new Claim();
        claim3.setDonationId(savedD3.getId());
        claim3.setDonorId(savedDonor1.getId());
        claim3.setReceiverId(savedReceiver1.getId());
        claim3.setReceiverName(savedReceiver1.getOrganizationName());
        claim3.setReceiverPhone(savedReceiver1.getPhone());
        claim3.setReceiverAddress(savedReceiver1.getAddress());
        claim3.setClaimedQuantity(30.0);
        claim3.setMeasurementUnit("kg");
        claim3.setStatus(ClaimStatus.COMPLETED);
        claim3.setQrVerified(true);
        claim3.setQrVerifiedAt(now.minus(25, ChronoUnit.HOURS));
        claim3.setPickedUpAt(now.minus(25, ChronoUnit.HOURS));
        claim3.setCompletedAt(now.minus(24, ChronoUnit.HOURS));
        claim3.setProposedPickupTime(now.minus(25, ChronoUnit.HOURS));
        claim3.setConfirmedPickupTime(now.minus(25, ChronoUnit.HOURS));
        Claim savedClaim3 = claimRepository.save(claim3);

        ImpactRecord impact1 = new ImpactRecord(
                savedD3.getId(), savedDonor1.getId(), savedReceiver1.getId(),
                FoodCategory.COOKED_FOOD, 30.0, "Brigade Road, Bangalore"
        );
        impact1.setClaimId(savedClaim3.getId());
        impactRecordRepository.save(impact1);

        // Donation 4: Partially Claimed Batch (40 kg -> 15 kg claimed with QR Passport + Volunteer Needed, 25 kg remaining!)
        Donation d4 = new Donation();
        d4.setDonorId(savedDonor2.getId());
        d4.setDonorName(savedDonor2.getOrganizationName());
        d4.setDonorPhone(savedDonor2.getPhone());
        d4.setFoodName("Ghee Rice, Paneer Masala & Chapati Packs");
        d4.setFoodDescription("Corporate conference catering surplus packed in hygienic meal boxes. Open for partial NGO splits.");
        d4.setFoodCategory(FoodCategory.COOKED_FOOD);
        d4.setQuantity(40.0);
        d4.setRemainingQuantity(25.0);
        d4.setAllocatedQuantity(15.0);
        d4.setAllocationCount(1);
        d4.setMeasurementUnit("kg");
        d4.setFoodImage("https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=800&auto=format&fit=crop");
        d4.setPreparationDateTime(now.minus(1, ChronoUnit.HOURS));
        d4.setPickupDeadline(now.plus(5, ChronoUnit.HOURS));
        d4.setPickupAddress("88, Koramangala 6th Block, Bangalore");
        d4.setLatitude(12.9380);
        d4.setLongitude(77.6220);
        d4.setFoodHandlingDetails("Hot-packed in sealed aluminum catering trays.");
        d4.setStorageInformation("Stored in clean insulated boxes.");
        d4.setStatus(DonationStatus.PARTIALLY_CLAIMED);
        d4.setSafetyIndicator(SafetyIndicator.LOW_RISK);
        d4.setSafetyNotes("Hot-hold maintained above 65°C. Supports partial batch splitting.");
        d4.setClaimedByReceiverId(savedReceiver2.getId());
        d4.setClaimedByReceiverName(savedReceiver2.getOrganizationName());
        Donation savedD4 = donationRepository.save(d4);

        Claim partialClaim = new Claim();
        partialClaim.setDonationId(savedD4.getId());
        partialClaim.setDonorId(savedDonor2.getId());
        partialClaim.setReceiverId(savedReceiver2.getId());
        partialClaim.setReceiverName(savedReceiver2.getOrganizationName());
        partialClaim.setReceiverPhone(savedReceiver2.getPhone());
        partialClaim.setReceiverAddress(savedReceiver2.getAddress());
        partialClaim.setClaimedQuantity(15.0);
        partialClaim.setMeasurementUnit("kg");
        partialClaim.setStatus(ClaimStatus.APPROVED);
        partialClaim.setProposedPickupTime(now.plus(1, ChronoUnit.HOURS));
        partialClaim.setConfirmedPickupTime(now.plus(1, ChronoUnit.HOURS));
        partialClaim.setNotes("Partial claim of 15 kg; requesting volunteer courier transport.");
        partialClaim.setVolunteerRequested(true);
        partialClaim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.REQUESTED);
        Claim savedPartialClaim = claimRepository.save(partialClaim);
        savedD4.getActiveClaimIds().add(savedPartialClaim.getId());
        donationRepository.save(savedD4);

        passportService.generatePassportForClaim(savedPartialClaim, savedD4);

        // Donation 5: Expired listing in Whitefield for realistic Heatmap bottleneck metrics
        Donation d5 = new Donation();
        d5.setDonorId(savedDonor2.getId());
        d5.setDonorName(savedDonor2.getOrganizationName());
        d5.setDonorPhone(savedDonor2.getPhone());
        d5.setFoodName("Cut Fruit & Salad Trays");
        d5.setFoodDescription("Chilled fruit platter surplus from evening reception.");
        d5.setFoodCategory(FoodCategory.FRUITS);
        d5.setQuantity(12.0);
        d5.setRemainingQuantity(12.0);
        d5.setMeasurementUnit("kg");
        d5.setPreparationDateTime(now.minus(12, ChronoUnit.HOURS));
        d5.setPickupDeadline(now.minus(2, ChronoUnit.HOURS));
        d5.setPickupAddress("ITPL Main Road, Whitefield, Bangalore");
        d5.setLatitude(12.9698);
        d5.setLongitude(77.7499);
        d5.setStatus(DonationStatus.EXPIRED);
        d5.setSafetyIndicator(SafetyIndicator.EXPIRED);
        d5.setSafetyNotes("Pickup deadline lapsed prior to NGO assignment.");
        donationRepository.save(d5);

        // Compute deterministic initial Trust Scores for all demo users
        trustScoreService.recalculateAndSave(savedDonor1.getId());
        trustScoreService.recalculateAndSave(savedDonor2.getId());
        trustScoreService.recalculateAndSave(savedReceiver1.getId());
        trustScoreService.recalculateAndSave(savedReceiver2.getId());
        trustScoreService.recalculateAndSave(savedVolunteer.getId());

        log.info("Demo seed data successfully initialized (including Partial Split, QR Passport, Volunteer Mission & Multi-Zone Heatmap data).");
    }

    private void ensureV2UpgradeAndBackfill() {
        // 1. Backfill partial-distribution fields on any legacy Donation documents
        for (Donation d : donationRepository.findAll()) {
            boolean dirty = false;
            if (d.getAllocatedQuantity() == null) {
                d.setAllocatedQuantity(0.0);
                dirty = true;
            }
            if (d.getAllocationCount() == null) {
                d.setAllocationCount(0);
                dirty = true;
            }
            if (d.getActiveClaimIds() == null) {
                d.setActiveClaimIds(new java.util.ArrayList<>());
                dirty = true;
            }
            if (d.getRemainingQuantity() == null) {
                double rem = (d.getStatus() == DonationStatus.AVAILABLE || d.getStatus() == DonationStatus.MATCHED)
                        ? Math.max(0.0, (d.getQuantity() != null ? d.getQuantity() : 0.0) - d.getAllocatedQuantity())
                        : 0.0;
                d.setRemainingQuantity(rem);
                dirty = true;
            }
            if (dirty) {
                donationRepository.save(d);
            }
        }

        // 2. Ensure demo Volunteer account + VolunteerProfile exist
        boolean volunteerCreated = false;
        User volunteer = userRepository.findByEmail("volunteer@annadhata.org").orElse(null);
        if (volunteer == null) {
            volunteer = new User("Karthik Hegde", "volunteer@annadhata.org", passwordEncoder.encode("volunteer123"), Role.ROLE_VOLUNTEER);
            volunteer.setOrganizationName("AnnaDhata Rapid Volunteer Squad");
            volunteer.setOrganizationType("Volunteer Courier");
            volunteer.setPhone("+91 99001 44556");
            volunteer.setAddress("22, Koramangala 4th Block, Bangalore");
            volunteer.setVerificationStatus(VerificationStatus.VERIFIED);
            volunteer.setLatitude(12.9352);
            volunteer.setLongitude(77.6245);
            volunteer = userRepository.save(volunteer);
            volunteerCreated = true;

            VolunteerProfile vp = new VolunteerProfile();
            vp.setUserId(volunteer.getId());
            vp.setUserName(volunteer.getName());
            vp.setPhone(volunteer.getPhone());
            vp.setVehicleType("Cargo Scooter / Van");
            vp.setMaxCapacityKg(40.0);
            vp.setServiceRadiusKm(25.0);
            vp.setAvailableForDispatch(true);
            vp.setLatitude(volunteer.getLatitude());
            vp.setLongitude(volunteer.getLongitude());
            vp.setAddress(volunteer.getAddress());
            vp.setCompletedDeliveriesCount(2);
            vp.setTotalKgTransported(38.0);
            volunteerProfileRepository.save(vp);
        }

        // 3. If upgrading from v1 and no active PARTIALLY_CLAIMED showcase exists yet, seed one cleanly
        Instant now = Instant.now();
        boolean hasActivePartial = donationRepository.findByStatus(DonationStatus.PARTIALLY_CLAIMED).stream()
                .anyMatch(d -> d.getPickupDeadline() != null && d.getPickupDeadline().isAfter(now));
        if (volunteerCreated && !hasActivePartial) {
            User donor = userRepository.findByEmail("royalpalace@hotel.com")
                    .or(() -> userRepository.findByRole(Role.ROLE_DONOR).stream().findFirst())
                    .orElse(null);
            User receiver = userRepository.findByEmail("annaseva@charity.org")
                    .or(() -> userRepository.findByRole(Role.ROLE_RECEIVER).stream().findFirst())
                    .orElse(null);
            if (donor != null && receiver != null) {
                Donation d4 = new Donation();
                d4.setDonorId(donor.getId());
                d4.setDonorName(donor.getOrganizationName() != null ? donor.getOrganizationName() : donor.getName());
                d4.setDonorPhone(donor.getPhone());
                d4.setFoodName("Ghee Rice, Paneer Masala & Chapati Packs");
                d4.setFoodDescription("Corporate conference catering surplus packed in hygienic meal boxes. Open for partial NGO splits.");
                d4.setFoodCategory(FoodCategory.COOKED_FOOD);
                d4.setQuantity(40.0);
                d4.setRemainingQuantity(25.0);
                d4.setAllocatedQuantity(15.0);
                d4.setAllocationCount(1);
                d4.setMeasurementUnit("kg");
                d4.setFoodImage("https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=800&auto=format&fit=crop");
                d4.setPreparationDateTime(now.minus(1, ChronoUnit.HOURS));
                d4.setPickupDeadline(now.plus(5, ChronoUnit.HOURS));
                d4.setPickupAddress("88, Koramangala 6th Block, Bangalore");
                d4.setLatitude(12.9380);
                d4.setLongitude(77.6220);
                d4.setFoodHandlingDetails("Hot-packed in sealed aluminum catering trays.");
                d4.setStorageInformation("Stored in clean insulated boxes.");
                d4.setStatus(DonationStatus.PARTIALLY_CLAIMED);
                d4.setSafetyIndicator(SafetyIndicator.LOW_RISK);
                d4.setSafetyNotes("Hot-hold maintained above 65°C. Supports partial batch splitting.");
                d4.setClaimedByReceiverId(receiver.getId());
                d4.setClaimedByReceiverName(receiver.getOrganizationName() != null ? receiver.getOrganizationName() : receiver.getName());
                Donation savedD4 = donationRepository.save(d4);

                Claim partialClaim = new Claim();
                partialClaim.setDonationId(savedD4.getId());
                partialClaim.setDonorId(donor.getId());
                partialClaim.setReceiverId(receiver.getId());
                partialClaim.setReceiverName(savedD4.getClaimedByReceiverName());
                partialClaim.setReceiverPhone(receiver.getPhone());
                partialClaim.setReceiverAddress(receiver.getAddress());
                partialClaim.setClaimedQuantity(15.0);
                partialClaim.setMeasurementUnit("kg");
                partialClaim.setStatus(ClaimStatus.APPROVED);
                partialClaim.setProposedPickupTime(now.plus(1, ChronoUnit.HOURS));
                partialClaim.setConfirmedPickupTime(now.plus(1, ChronoUnit.HOURS));
                partialClaim.setNotes("Partial claim of 15 kg; requesting volunteer courier transport.");
                partialClaim.setVolunteerRequested(true);
                partialClaim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.REQUESTED);
                Claim savedPartialClaim = claimRepository.save(partialClaim);
                savedD4.getActiveClaimIds().add(savedPartialClaim.getId());
                donationRepository.save(savedD4);
                passportService.generatePassportForClaim(savedPartialClaim, savedD4);
            }
        }

        // 4. Backfill initial Trust Scores for any users missing trustScore
        for (User u : userRepository.findAll()) {
            if (u.getTrustScore() == null) {
                trustScoreService.recalculateAndSave(u.getId());
            }
        }
    }
}
