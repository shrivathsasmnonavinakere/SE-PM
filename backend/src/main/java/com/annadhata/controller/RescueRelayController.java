package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.RescueRelayActionRequest;
import com.annadhata.dto.RescueRelayResponse;
import com.annadhata.enums.Role;
import com.annadhata.model.User;
import com.annadhata.service.RescueRelayService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescue-relay")
@Tag(name = "Rescue Relay", description = "Missed pickup detection, donor-approved release, and urgent NGO re-matching")
public class RescueRelayController {

    private final RescueRelayService rescueRelayService;

    public RescueRelayController(RescueRelayService rescueRelayService) {
        this.rescueRelayService = rescueRelayService;
    }

    @GetMapping("/my")
    @Operation(summary = "Get Rescue Relay events relevant to the authenticated user")
    public ResponseEntity<ApiResponse<List<RescueRelayResponse>>> getMyRescueRelays() {
        User user = SecurityUtils.getCurrentUser();
        List<RescueRelayResponse> list;
        if (user.getRole() == Role.ROLE_ADMIN) {
            list = rescueRelayService.getAllRelays();
        } else if (user.getRole() == Role.ROLE_DONOR) {
            list = rescueRelayService.getDonorRelays(user.getId());
        } else {
            list = rescueRelayService.getReceiverRelays(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Scan approved pickups for overdue/missed handovers")
    public ResponseEntity<ApiResponse<List<RescueRelayResponse>>> scanMissedPickups(
            @RequestParam(defaultValue = "15") int graceMinutes) {
        List<RescueRelayResponse> detected = rescueRelayService.detectMissedPickups(graceMinutes);
        return ResponseEntity.ok(ApiResponse.success("Rescue Relay scan complete", detected));
    }

    @PostMapping("/claims/{claimId}/release")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Donor approves releasing an overdue allocation back to surplus pool and finding alternative NGOs")
    public ResponseEntity<ApiResponse<RescueRelayResponse>> approveReleaseAndRematch(
            @PathVariable String claimId,
            @RequestBody(required = false) RescueRelayActionRequest request) {
        User actor = SecurityUtils.getCurrentUser();
        RescueRelayResponse response = rescueRelayService.approveReleaseAndRematch(claimId, request, actor);
        return ResponseEntity.ok(ApiResponse.success("Allocation released and alternative NGOs notified!", response));
    }

    @PostMapping("/claims/{claimId}/dismiss")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Dismiss Rescue Relay warning and keep existing allocation")
    public ResponseEntity<ApiResponse<RescueRelayResponse>> dismissRelay(
            @PathVariable String claimId,
            @RequestBody(required = false) Map<String, String> body) {
        User actor = SecurityUtils.getCurrentUser();
        String notes = body != null ? body.get("notes") : null;
        RescueRelayResponse response = rescueRelayService.dismissRelay(claimId, notes, actor);
        return ResponseEntity.ok(ApiResponse.success("Rescue Relay alert dismissed", response));
    }
}

