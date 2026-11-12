package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.ImpactSummaryResponse;
import com.annadhata.service.ImpactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impact")
@Tag(name = "Impact & Sustainability Tracking", description = "Endpoints for environmental impact metrics, meals saved, and waste diversion")
public class ImpactController {

    private final ImpactService impactService;

    public ImpactController(ImpactService impactService) {
        this.impactService = impactService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get platform sustainability impact summary")
    public ResponseEntity<ApiResponse<ImpactSummaryResponse>> getImpactSummary() {
        ImpactSummaryResponse response = impactService.getPlatformImpactSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/summary")
    @Operation(summary = "Public endpoint for impact summary (for landing page)")
    public ResponseEntity<ApiResponse<ImpactSummaryResponse>> getPublicImpactSummary() {
        ImpactSummaryResponse response = impactService.getPlatformImpactSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
