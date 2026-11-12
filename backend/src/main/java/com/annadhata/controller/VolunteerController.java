package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.VolunteerDashboardResponse;
import com.annadhata.dto.VolunteerProfileUpdateRequest;
import com.annadhata.dto.VolunteerTaskResponse;
import com.annadhata.model.User;
import com.annadhata.model.VolunteerProfile;
import com.annadhata.service.VolunteerService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
@Tag(name = "Volunteer Network", description = "Volunteer dispatch missions, vehicle availability, and last-mile food delivery")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Get Volunteer Portal dashboard with active missions and open pickup tasks")
    public ResponseEntity<ApiResponse<VolunteerDashboardResponse>> getDashboard() {
        User volunteer = SecurityUtils.getCurrentUser();
        VolunteerDashboardResponse response = volunteerService.getVolunteerDashboard(volunteer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Get current volunteer's dispatch profile")
    public ResponseEntity<ApiResponse<VolunteerProfile>> getProfile() {
        User volunteer = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(volunteerService.getOrCreateProfile(volunteer)));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Update volunteer vehicle, capacity, radius, and availability toggle")
    public ResponseEntity<ApiResponse<VolunteerProfile>> updateProfile(@Valid @RequestBody VolunteerProfileUpdateRequest request) {
        User volunteer = SecurityUtils.getCurrentUser();
        VolunteerProfile updated = volunteerService.updateProfile(request, volunteer);
        return ResponseEntity.ok(ApiResponse.success("Volunteer dispatch preferences updated", updated));
    }

    @GetMapping("/tasks/available")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Discover open approved food delivery missions nearby")
    public ResponseEntity<ApiResponse<List<VolunteerTaskResponse>>> getAvailableTasks() {
        User volunteer = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(volunteerService.getAvailableTasks(volunteer)));
    }

    @GetMapping("/tasks/my")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Get missions assigned to the authenticated volunteer")
    public ResponseEntity<ApiResponse<List<VolunteerTaskResponse>>> getMyMissions() {
        User volunteer = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(volunteerService.getMyMissions(volunteer)));
    }

    @PostMapping("/tasks/{claimId}/accept")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Atomically accept an open food pickup and delivery mission")
    public ResponseEntity<ApiResponse<VolunteerTaskResponse>> acceptTask(@PathVariable String claimId) {
        User volunteer = SecurityUtils.getCurrentUser();
        VolunteerTaskResponse task = volunteerService.acceptTask(claimId, volunteer);
        return ResponseEntity.ok(ApiResponse.success("Mission accepted! Donor and receiver have been notified.", task));
    }

    @PostMapping("/tasks/{claimId}/release")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Release an assigned delivery mission before pickup")
    public ResponseEntity<ApiResponse<VolunteerTaskResponse>> releaseTask(@PathVariable String claimId) {
        User volunteer = SecurityUtils.getCurrentUser();
        VolunteerTaskResponse task = volunteerService.releaseTask(claimId, volunteer);
        return ResponseEntity.ok(ApiResponse.success("Mission released back to open dispatch pool.", task));
    }

    @PostMapping("/tasks/{claimId}/deliver")
    @PreAuthorize("hasAnyAuthority('ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Confirm food delivered safely to destination NGO")
    public ResponseEntity<ApiResponse<VolunteerTaskResponse>> markDelivered(@PathVariable String claimId) {
        User volunteer = SecurityUtils.getCurrentUser();
        VolunteerTaskResponse task = volunteerService.markDelivered(claimId, volunteer);
        return ResponseEntity.ok(ApiResponse.success("Delivery completed! Impact and trust score updated.", task));
    }

    @PostMapping("/claims/{claimId}/request")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Request a volunteer courier for an approved claim")
    public ResponseEntity<ApiResponse<VolunteerTaskResponse>> requestVolunteer(@PathVariable String claimId) {
        User caller = SecurityUtils.getCurrentUser();
        VolunteerTaskResponse task = volunteerService.requestVolunteerSupport(claimId, caller);
        return ResponseEntity.ok(ApiResponse.success("Volunteer dispatch request broadcast to nearby volunteers.", task));
    }
}

