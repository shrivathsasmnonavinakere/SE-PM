package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.CreateDonationRequest;
import com.annadhata.dto.DonationResponse;
import com.annadhata.dto.MatchRecommendationResponse;
import com.annadhata.dto.UpdateDonationRequest;
import com.annadhata.enums.FoodCategory;
import com.annadhata.matching.MatchingService;
import com.annadhata.model.User;
import com.annadhata.service.DonationService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@Tag(name = "Food Donation Management", description = "Endpoints for creating, managing, browsing, and matching food donations")
public class DonationController {

    private final DonationService donationService;
    private final MatchingService matchingService;

    public DonationController(DonationService donationService, MatchingService matchingService) {
        this.donationService = donationService;
        this.matchingService = matchingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Create a new food donation listing")
    public ResponseEntity<ApiResponse<DonationResponse>> createDonation(@Valid @RequestBody CreateDonationRequest request) {
        User donor = SecurityUtils.getCurrentUser();
        DonationResponse response = donationService.createDonation(request, donor);
        return ResponseEntity.ok(ApiResponse.success("Food donation created and matched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Update an existing food donation listing")
    public ResponseEntity<ApiResponse<DonationResponse>> updateDonation(
            @PathVariable String id,
            @RequestBody UpdateDonationRequest request) {
        User user = SecurityUtils.getCurrentUser();
        DonationResponse response = donationService.updateDonation(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Donation updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Cancel or delete a food donation")
    public ResponseEntity<ApiResponse<DonationResponse>> cancelDonation(@PathVariable String id) {
        User user = SecurityUtils.getCurrentUser();
        DonationResponse response = donationService.cancelDonation(id, user);
        return ResponseEntity.ok(ApiResponse.success("Donation cancelled successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get donation details by ID")
    public ResponseEntity<ApiResponse<DonationResponse>> getDonationById(
            @PathVariable String id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        DonationResponse response = donationService.getDonationById(id, lat, lon);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Public get donation details")
    public ResponseEntity<ApiResponse<DonationResponse>> getPublicDonationById(@PathVariable String id) {
        DonationResponse response = donationService.getDonationById(id, null, null);
        response.setDonorPhone(null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/available")
    @Operation(summary = "Public discovery of active food donations with location filter")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getPublicAvailableDonations(
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double maxDistanceKm) {
        List<DonationResponse> list = donationService.getAvailableDonations(category, lat, lon, maxDistanceKm);
        list.forEach(d -> d.setDonorPhone(null));
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/available")
    @Operation(summary = "Discover available food donations for receivers")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getAvailableDonations(
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double maxDistanceKm) {
        User user = SecurityUtils.getCurrentUser();
        Double uLat = lat != null ? lat : user.getLatitude();
        Double uLon = lon != null ? lon : user.getLongitude();
        List<DonationResponse> list = donationService.getAvailableDonations(category, uLat, uLon, maxDistanceKm);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get all donations created by the authenticated donor")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getMyDonations() {
        User donor = SecurityUtils.getCurrentUser();
        List<DonationResponse> list = donationService.getDonorDonations(donor.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}/matches")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get AI smart matching recommendations for this donation")
    public ResponseEntity<ApiResponse<List<MatchRecommendationResponse>>> getDonationMatches(@PathVariable String id) {
        User currentUser = SecurityUtils.getCurrentUser();
        DonationResponse donation = donationService.getDonationById(id, null, null);
        if (!donation.getDonorId().equals(currentUser.getId()) && currentUser.getRole() != com.annadhata.enums.Role.ROLE_ADMIN) {
            throw new com.annadhata.exception.ForbiddenException("You are not authorized to view matches for another donor's listing");
        }
        List<MatchRecommendationResponse> matches = matchingService.findMatchesForDonation(id);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }
}
