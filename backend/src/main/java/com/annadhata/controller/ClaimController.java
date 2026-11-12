package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.ClaimResponse;
import com.annadhata.dto.CreateClaimRequest;
import com.annadhata.enums.Role;
import com.annadhata.model.User;
import com.annadhata.service.ClaimService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claim & Pickup Management", description = "Endpoints for claiming food donations, approving claims, and updating pickup status")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Submit a claim for an available food donation")
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        User receiver = SecurityUtils.getCurrentUser();
        ClaimResponse response = claimService.createClaim(request, receiver);
        return ResponseEntity.ok(ApiResponse.success("Claim submitted successfully. Awaiting donor confirmation.", response));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Approve a claim request and confirm pickup schedule")
    public ResponseEntity<ApiResponse<ClaimResponse>> approveClaim(@PathVariable String id) {
        User donor = SecurityUtils.getCurrentUser();
        ClaimResponse response = claimService.approveClaim(id, donor);
        return ResponseEntity.ok(ApiResponse.success("Claim approved. Pickup window confirmed.", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Reject a claim request with reason")
    public ResponseEntity<ApiResponse<ClaimResponse>> rejectClaim(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        User donor = SecurityUtils.getCurrentUser();
        String reason = body != null ? body.get("reason") : "Schedule incompatibility";
        ClaimResponse response = claimService.rejectClaim(id, reason, donor);
        return ResponseEntity.ok(ApiResponse.success("Claim rejected", response));
    }

    @PutMapping("/{id}/picked-up")
    @Operation(summary = "Confirm that the food has been collected and is in transit")
    public ResponseEntity<ApiResponse<ClaimResponse>> markPickedUp(@PathVariable String id) {
        User user = SecurityUtils.getCurrentUser();
        ClaimResponse response = claimService.markPickedUp(id, user);
        return ResponseEntity.ok(ApiResponse.success("Pickup confirmed. Food in safe transit.", response));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Confirm successful collection and mark redistribution as completed")
    public ResponseEntity<ApiResponse<ClaimResponse>> markCompleted(@PathVariable String id) {
        User user = SecurityUtils.getCurrentUser();
        ClaimResponse response = claimService.markCompleted(id, user);
        return ResponseEntity.ok(ApiResponse.success("Donation completed! Sustainability impact successfully recorded.", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get claims relevant to the authenticated user")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getMyClaims() {
        User user = SecurityUtils.getCurrentUser();
        List<ClaimResponse> list;
        if (user.getRole() == Role.ROLE_RECEIVER) {
            list = claimService.getReceiverClaims(user.getId());
        } else {
            list = claimService.getDonorClaims(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get all claim requests for a specific donation")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getDonationClaims(@PathVariable String donationId) {
        User user = SecurityUtils.getCurrentUser();
        List<ClaimResponse> list = claimService.getDonationClaims(donationId, user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
