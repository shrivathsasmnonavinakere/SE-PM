package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.DonationResponse;
import com.annadhata.dto.UserProfileResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.exception.BadRequestException;
import com.annadhata.model.AuditLog;
import com.annadhata.model.User;
import com.annadhata.service.AdminService;
import com.annadhata.service.DonationService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Platform Administration", description = "Endpoints for user moderation, organisation verification, donations oversight, and audit logs")
public class AdminController {

    private final AdminService adminService;
    private final DonationService donationService;

    public AdminController(AdminService adminService, DonationService donationService) {
        this.adminService = adminService;
        this.donationService = donationService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all registered users across all roles")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        List<UserProfileResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/donations")
    @Operation(summary = "Get all food donations across all statuses for admin oversight")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getAllDonations() {
        List<DonationResponse> donations = donationService.getAllDonations();
        return ResponseEntity.ok(ApiResponse.success(donations));
    }

    @PutMapping("/users/{id}/verify")
    @Operation(summary = "Approve or reject organisation verification request")
    public ResponseEntity<ApiResponse<UserProfileResponse>> verifyOrganisation(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("approve")) {
            throw new BadRequestException("Field 'approve' (boolean) is required");
        }
        User admin = SecurityUtils.getCurrentUser();
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        Object rawNotes = body.get("notes");
        String notes = rawNotes != null ? String.valueOf(rawNotes) : "Reviewed by Admin";
        UserProfileResponse updated = adminService.verifyOrganisation(id, approve, notes, admin);
        return ResponseEntity.ok(ApiResponse.success("Verification status updated", updated));
    }

    @PutMapping("/users/{id}/toggle-active")
    @Operation(summary = "Suspend or activate a user account")
    public ResponseEntity<ApiResponse<UserProfileResponse>> toggleUserActive(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("active")) {
            throw new BadRequestException("Field 'active' (boolean) is required");
        }
        User admin = SecurityUtils.getCurrentUser();
        boolean active = Boolean.TRUE.equals(body.get("active"));
        UserProfileResponse updated = adminService.toggleUserActive(id, active, admin);
        return ResponseEntity.ok(ApiResponse.success("User account status updated", updated));
    }

    @PutMapping("/donations/{id}/moderate")
    @Operation(summary = "Moderate or flag an inappropriate donation listing")
    public ResponseEntity<ApiResponse<Void>> moderateDonation(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        User admin = SecurityUtils.getCurrentUser();
        String statusStr = body != null ? body.getOrDefault("status", "CANCELLED") : "CANCELLED";
        DonationStatus status;
        try {
            status = DonationStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid moderation status: " + statusStr);
        }
        String reason = body != null ? body.getOrDefault("reason", "Admin policy review") : "Admin policy review";
        adminService.moderateDonation(id, status, reason, admin);
        return ResponseEntity.ok(ApiResponse.success("Donation moderated successfully", null));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "View security and administrative audit logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = adminService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
