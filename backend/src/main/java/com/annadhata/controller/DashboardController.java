package com.annadhata.controller;

import com.annadhata.dto.AdminDashboardResponse;
import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.DonorDashboardResponse;
import com.annadhata.dto.ReceiverDashboardResponse;
import com.annadhata.model.User;
import com.annadhata.service.DashboardService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Dashboards", description = "Role-based dashboards with live database statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/donor/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get Donor Dashboard metrics, recent donations, and pending claims")
    public ResponseEntity<ApiResponse<DonorDashboardResponse>> getDonorDashboard() {
        User donor = SecurityUtils.getCurrentUser();
        DonorDashboardResponse response = dashboardService.getDonorDashboard(donor.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/receiver/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Get Receiver Dashboard metrics, recommendations, and active claims")
    public ResponseEntity<ApiResponse<ReceiverDashboardResponse>> getReceiverDashboard(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        User receiver = SecurityUtils.getCurrentUser();
        Double uLat = lat != null ? lat : receiver.getLatitude();
        Double uLon = lon != null ? lon : receiver.getLongitude();
        ReceiverDashboardResponse response = dashboardService.getReceiverDashboard(receiver.getId(), uLat, uLon);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get Admin Platform-wide Dashboard with verification queues and safety alerts")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
