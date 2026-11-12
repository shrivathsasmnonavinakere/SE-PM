package com.annadhata.service;

import com.annadhata.dto.UserProfileResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.NotificationType;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.exception.ResourceNotFoundException;
import com.annadhata.model.AuditLog;
import com.annadhata.model.Donation;
import com.annadhata.model.User;
import com.annadhata.model.VerificationRequest;
import com.annadhata.repository.AuditLogRepository;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.UserRepository;
import com.annadhata.repository.VerificationRequestRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    public AdminService(UserRepository userRepository,
                        DonationRepository donationRepository,
                        VerificationRequestRepository verificationRequestRepository,
                        AuditLogRepository auditLogRepository,
                        NotificationService notificationService,
                        AuthService authService) {
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    public UserProfileResponse verifyOrganisation(String userId, boolean approve, String notes, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setVerificationStatus(approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
        User saved = userRepository.save(user);

        // Update or create verification request record
        VerificationRequest request = verificationRequestRepository.findByUserId(userId)
                .orElse(new VerificationRequest());
        request.setUserId(user.getId());
        request.setUserEmail(user.getEmail());
        request.setOrganizationName(user.getOrganizationName());
        request.setOrganizationType(user.getOrganizationType());
        request.setRegistrationNumber(user.getRegistrationNumber());
        request.setDocumentUrls(user.getVerificationDocuments());
        request.setStatus(user.getVerificationStatus());
        request.setAdminReviewNotes(notes);
        request.setReviewedByAdminId(admin.getId());
        request.setReviewedAt(Instant.now());
        verificationRequestRepository.save(request);

        // Log audit
        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(),
                approve ? "APPROVE_ORGANISATION" : "REJECT_ORGANISATION",
                "User", user.getId(), notes
        ));

        // Notify organisation
        notificationService.sendNotification(
                user.getId(),
                approve ? "Organisation Verification Approved!" : "Organisation Verification Update",
                approve ? "Your organisation has been verified by the AnnaDhata administration. You can now claim food donations." :
                        "Your verification was not approved: " + notes,
                NotificationType.CLAIM_APPROVED,
                user.getId()
        );

        return authService.mapToProfileResponse(saved);
    }

    public UserProfileResponse toggleUserActive(String userId, boolean active, User admin) {
        if (userId.equals(admin.getId()) && !active) {
            throw new com.annadhata.exception.BadRequestException("Administrators cannot suspend their own active account");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setActive(active);
        User saved = userRepository.save(user);

        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(),
                active ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                "User", user.getId(), "Account active status set to " + active
        ));

        return authService.mapToProfileResponse(saved);
    }

    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    public void moderateDonation(String donationId, DonationStatus status, String reason, User admin) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));

        donation.setStatus(status);
        donationRepository.save(donation);

        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(),
                "MODERATE_DONATION", "Donation", donation.getId(),
                "Status set to " + status + ". Reason: " + reason
        ));

        notificationService.sendNotification(
                donation.getDonorId(),
                "Listing Moderation Notice",
                "Your listing '" + donation.getFoodName() + "' was moderated: " + reason,
                NotificationType.EXPIRY_WARNING,
                donation.getId()
        );
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }
}
