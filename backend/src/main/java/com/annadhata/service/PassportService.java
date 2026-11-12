package com.annadhata.service;

import com.annadhata.dto.FoodPassportResponse;
import com.annadhata.dto.VerifyPassportRequest;
import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.PassportStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.Role;
import com.annadhata.enums.VolunteerAssignmentStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.exception.ForbiddenException;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.Claim;
import com.annadhata.model.Donation;
import com.annadhata.model.FoodPassport;
import com.annadhata.model.User;
import com.annadhata.repository.ClaimRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.FoodPassportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PassportService {

    private static final Logger log = LoggerFactory.getLogger(PassportService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FoodPassportRepository passportRepository;
    private final ClaimRepository claimRepository;
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;
    private final TrustScoreService trustScoreService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${annadhata.jwt.secret:dev-only-change-this-before-production-20261112}")
    private String hmacSecret;

    public PassportService(FoodPassportRepository passportRepository,
                           ClaimRepository claimRepository,
                           DonationRepository donationRepository,
                           NotificationService notificationService,
                           TrustScoreService trustScoreService) {
        this.passportRepository = passportRepository;
        this.claimRepository = claimRepository;
        this.donationRepository = donationRepository;
        this.notificationService = notificationService;
        this.trustScoreService = trustScoreService;
    }

    public FoodPassportResponse generatePassportForClaim(Claim claim, Donation donation) {
        Optional<FoodPassport> existing = passportRepository.findByClaimId(claim.getId());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        byte[] tokenBytes = new byte[6];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String randomHex = bytesToHex(tokenBytes).toUpperCase();
        String passportCode = "AD-" + randomHex.substring(0, 4) + "-" + randomHex.substring(4, 8) + "-" + randomHex.substring(8);

        String hmacSig = computeHmac(claim.getId(), donation.getId(), claim.getReceiverId(), claim.getClaimedQuantity(), passportCode);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("passportCode", passportCode);
        payload.put("claimId", claim.getId());
        payload.put("donationId", donation.getId());
        payload.put("foodName", donation.getFoodName());
        payload.put("qty", claim.getClaimedQuantity());
        payload.put("unit", claim.getMeasurementUnit());
        payload.put("receiver", claim.getReceiverName());
        payload.put("safety", donation.getSafetyIndicator() != null ? donation.getSafetyIndicator().name() : "LOW_RISK");
        payload.put("sig", hmacSig);

        String qrPayloadJson;
        try {
            qrPayloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            qrPayloadJson = passportCode;
        }

        String qrBase64 = generateQrPngDataUrl(qrPayloadJson);

        FoodPassport passport = new FoodPassport();
        passport.setClaimId(claim.getId());
        passport.setDonationId(donation.getId());
        passport.setDonorId(donation.getDonorId());
        passport.setReceiverId(claim.getReceiverId());
        passport.setDonorName(donation.getDonorName());
        passport.setReceiverName(claim.getReceiverName());
        passport.setFoodName(donation.getFoodName());
        passport.setAllocatedQuantity(claim.getClaimedQuantity());
        passport.setMeasurementUnit(claim.getMeasurementUnit());
        passport.setPassportCode(passportCode);
        passport.setHmacSignature(hmacSig);
        passport.setQrPayloadJson(qrPayloadJson);
        passport.setQrImageBase64(qrBase64);
        passport.setStatus(PassportStatus.ACTIVE);
        passport.setSafetyIndicator(donation.getSafetyIndicator());
        passport.setIssuedAt(Instant.now());
        passport.setExpiresAt(donation.getPickupDeadline() != null
                ? donation.getPickupDeadline().plusSeconds(900)
                : Instant.now().plusSeconds(21600));

        FoodPassport saved = passportRepository.save(passport);

        claim.setPassportId(saved.getId());
        claim.setPassportCode(saved.getPassportCode());
        claimRepository.save(claim);

        return mapToResponse(saved);
    }

    public FoodPassportResponse getPassportByClaimId(String claimId, User caller) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        boolean authorized = caller.getRole() == Role.ROLE_ADMIN
                || claim.getDonorId().equals(caller.getId())
                || claim.getReceiverId().equals(caller.getId())
                || (claim.getAssignedVolunteerId() != null && claim.getAssignedVolunteerId().equals(caller.getId()));

        if (!authorized) {
            throw new ForbiddenException("You are not authorized to access this Food Passport");
        }

        FoodPassport passport = passportRepository.findByClaimId(claimId)
                .orElseGet(() -> {
                    if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.PICKED_UP) {
                        Donation donation = donationRepository.findById(claim.getDonationId())
                                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
                        FoodPassportResponse created = generatePassportForClaim(claim, donation);
                        return passportRepository.findById(created.getId()).orElseThrow();
                    }
                    throw new BadRequestException("Food Passport is only generated once a claim is APPROVED");
                });

        return mapToResponse(passport);
    }

    public synchronized FoodPassportResponse verifyPassport(VerifyPassportRequest request, User verifier) {
        String rawInput = request.getPassportCode().trim();
        String codeToLookup = rawInput;
        String suppliedSignature = null;

        if (rawInput.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(rawInput);
                if (node.has("passportCode")) {
                    codeToLookup = node.get("passportCode").asText();
                }
                if (node.has("sig")) {
                    suppliedSignature = node.get("sig").asText();
                }
            } catch (Exception ex) {
                throw new BadRequestException("Invalid QR Food Passport JSON payload");
            }
        }

        final String finalLookupCode = codeToLookup;
        FoodPassport passport = passportRepository.findByPassportCode(finalLookupCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unrecognized QR Food Passport code: " + finalLookupCode));

        Claim claim = claimRepository.findById(passport.getClaimId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated claim not found"));

        boolean canVerify = verifier.getRole() == Role.ROLE_ADMIN
                || passport.getDonorId().equals(verifier.getId())
                || (claim.getAssignedVolunteerId() != null && claim.getAssignedVolunteerId().equals(verifier.getId()));

        if (!canVerify) {
            throw new ForbiddenException("Only the food donor, assigned volunteer, or administrator can verify pickup handover.");
        }

        // Verify cryptographic HMAC signature integrity
        String expectedHmac = computeHmac(claim.getId(), passport.getDonationId(), passport.getReceiverId(),
                passport.getAllocatedQuantity(), passport.getPassportCode());
        if (suppliedSignature != null && !MessageDigest.isEqual(
                expectedHmac.getBytes(StandardCharsets.UTF_8),
                suppliedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenException("QR Food Passport cryptographic signature verification failed!");
        }
        if (passport.getHmacSignature() != null && !MessageDigest.isEqual(
                expectedHmac.getBytes(StandardCharsets.UTF_8),
                passport.getHmacSignature().getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenException("Stored Food Passport signature mismatch!");
        }

        if (passport.getStatus() == PassportStatus.VERIFIED_PICKUP) {
            throw new BadRequestException("This Food Passport has already been scanned and verified at " + passport.getVerifiedAt());
        }
        if (passport.getStatus() == PassportStatus.REVOKED) {
            throw new BadRequestException("This Food Passport was revoked (allocation released or cancelled)");
        }
        Instant now = Instant.now();
        if (passport.getExpiresAt() != null && passport.getExpiresAt().isBefore(now)) {
            passport.setStatus(PassportStatus.EXPIRED);
            passportRepository.save(passport);
            throw new BadRequestException("This Food Passport has expired past pickup window");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Cannot verify pickup for claim in status: " + claim.getStatus());
        }

        // Mark passport verified
        passport.setStatus(PassportStatus.VERIFIED_PICKUP);
        passport.setVerifiedAt(now);
        passport.setVerifiedByUserId(verifier.getId());
        passport.setVerifiedByName(verifier.getOrganizationName() != null ? verifier.getOrganizationName() : verifier.getName());
        passport.setVerificationNotes(request.getVerificationNotes() != null ? request.getVerificationNotes() : "QR Food Passport verified at pickup handover");
        FoodPassport savedPassport = passportRepository.save(passport);

        // Transition Claim to PICKED_UP
        claim.setStatus(ClaimStatus.PICKED_UP);
        claim.setQrVerified(true);
        claim.setQrVerifiedAt(now);
        claim.setPickedUpAt(now);
        if (claim.getRescueRelayStatus() == RescueRelayStatus.AWAITING_DONOR_APPROVAL
                || claim.getRescueRelayStatus() == RescueRelayStatus.MISSED_DETECTED) {
            claim.setRescueRelayStatus(RescueRelayStatus.DISMISSED);
        }
        if (claim.getAssignedVolunteerId() != null) {
            claim.setVolunteerAssignmentStatus(VolunteerAssignmentStatus.PICKED_UP);
        }
        claimRepository.save(claim);

        // Update Donation if all allocated units are picked up / remaining <= 0
        Donation donation = donationRepository.findById(claim.getDonationId()).orElse(null);
        if (donation != null && donation.getRemainingQuantity() <= 0.001) {
            List<Claim> allClaims = claimRepository.findByDonationId(donation.getId());
            boolean anyStillWaiting = allClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);
            if (!anyStillWaiting && donation.getStatus() == DonationStatus.CLAIMED) {
                donation.setStatus(DonationStatus.PICKED_UP);
                donationRepository.save(donation);
            }
        }

        notificationService.sendNotification(
                claim.getReceiverId(),
                "QR Food Passport Verified!",
                String.format("Pickup for '%s' (%.1f %s) was verified via Food Passport %s. Safe delivery in progress!",
                        passport.getFoodName(), passport.getAllocatedQuantity(), passport.getMeasurementUnit(), passport.getPassportCode()),
                NotificationType.PASSPORT_VERIFIED,
                claim.getId()
        );
        notificationService.sendNotification(
                claim.getDonorId(),
                "Pickup Handover Verified via QR Passport",
                String.format("%s pickup verified via QR Passport %s by %s.",
                        passport.getFoodName(), passport.getPassportCode(), savedPassport.getVerifiedByName()),
                NotificationType.PASSPORT_VERIFIED,
                claim.getId()
        );

        trustScoreService.recalculateAndSave(claim.getReceiverId());
        if (claim.getAssignedVolunteerId() != null) {
            trustScoreService.recalculateAndSave(claim.getAssignedVolunteerId());
        }

        return mapToResponse(savedPassport);
    }

    public void revokePassportForClaim(String claimId, String reason) {
        passportRepository.findByClaimId(claimId).ifPresent(passport -> {
            if (passport.getStatus() == PassportStatus.ACTIVE) {
                passport.setStatus(PassportStatus.REVOKED);
                passport.setVerificationNotes(reason);
                passportRepository.save(passport);
            }
        });
    }

    public String computeHmac(String claimId, String donationId, String receiverId, Double qty, String passportCode) {
        try {
            String message = String.join("|",
                    String.valueOf(claimId),
                    String.valueOf(donationId),
                    String.valueOf(receiverId),
                    String.format("%.2f", qty != null ? qty : 0.0),
                    String.valueOf(passportCode));
            Mac mac = Mac.getInstance("HmacSHA256");
            String keySeed = hmacSecret != null && !hmacSecret.isEmpty() ? hmacSecret : "AnnaDhataDefaultPassportSecretKey2026";
            SecretKeySpec secretKey = new SecretKeySpec(keySeed.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac).substring(0, 16);
        } catch (Exception ex) {
            log.warn("HMAC generation fallback: {}", ex.getMessage());
            return Integer.toHexString((claimId + passportCode).hashCode());
        }
    }

    private String generateQrPngDataUrl(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 260, 260);
            ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutput);
            String base64 = Base64.getEncoder().encodeToString(pngOutput.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception ex) {
            log.warn("Could not generate QR PNG image: {}", ex.getMessage());
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public FoodPassportResponse mapToResponse(FoodPassport p) {
        FoodPassportResponse r = new FoodPassportResponse();
        r.setId(p.getId());
        r.setClaimId(p.getClaimId());
        r.setDonationId(p.getDonationId());
        r.setDonorId(p.getDonorId());
        r.setReceiverId(p.getReceiverId());
        r.setDonorName(p.getDonorName());
        r.setReceiverName(p.getReceiverName());
        r.setFoodName(p.getFoodName());
        r.setAllocatedQuantity(p.getAllocatedQuantity());
        r.setMeasurementUnit(p.getMeasurementUnit());
        r.setPassportCode(p.getPassportCode());
        r.setHmacSignature(p.getHmacSignature());
        r.setQrPayloadJson(p.getQrPayloadJson());
        r.setQrImageBase64(p.getQrImageBase64());
        r.setStatus(p.getStatus());
        r.setSafetyIndicator(p.getSafetyIndicator());
        r.setIssuedAt(p.getIssuedAt());
        r.setExpiresAt(p.getExpiresAt());
        r.setVerifiedAt(p.getVerifiedAt());
        r.setVerifiedByUserId(p.getVerifiedByUserId());
        r.setVerifiedByName(p.getVerifiedByName());
        r.setVerificationNotes(p.getVerificationNotes());
        return r;
    }
}
