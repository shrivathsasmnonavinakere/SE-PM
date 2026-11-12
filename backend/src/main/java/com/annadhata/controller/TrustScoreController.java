package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.TrustScoreResponse;
import com.annadhata.model.User;
import com.annadhata.service.TrustScoreService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trust")
@Tag(name = "Trust & Reliability Score", description = "Transparent reliability scoring for donors, receivers, and volunteers")
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    public TrustScoreController(TrustScoreService trustScoreService) {
        this.trustScoreService = trustScoreService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get transparent reliability score and factor breakdown for current authenticated user")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getMyTrustScore() {
        User currentUser = SecurityUtils.getCurrentUser();
        TrustScoreResponse response = trustScoreService.getOrCalculateTrustScore(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "View transparent reliability breakdown for a donor, receiver, or volunteer")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getUserTrustScore(@PathVariable String userId) {
        TrustScoreResponse response = trustScoreService.getOrCalculateTrustScore(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/recalculate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Force recalculation of user reliability metrics")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> recalculate(@PathVariable String userId) {
        TrustScoreResponse response = trustScoreService.recalculateAndSave(userId);
        return ResponseEntity.ok(ApiResponse.success("Trust score recalculated successfully", response));
    }
}

