package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.FoodPassportResponse;
import com.annadhata.dto.VerifyPassportRequest;
import com.annadhata.model.User;
import com.annadhata.service.PassportService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passports")
@Tag(name = "QR Food Passport", description = "Cryptographic QR Food Passports for verified pickup handovers")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @GetMapping("/claim/{claimId}")
    @Operation(summary = "Retrieve generated QR Food Passport for an approved claim")
    public ResponseEntity<ApiResponse<FoodPassportResponse>> getPassportForClaim(@PathVariable String claimId) {
        User caller = SecurityUtils.getCurrentUser();
        FoodPassportResponse passport = passportService.getPassportByClaimId(claimId, caller);
        return ResponseEntity.ok(ApiResponse.success(passport));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_VOLUNTEER', 'ROLE_ADMIN')")
    @Operation(summary = "Scan or verify a QR Food Passport code at food handover")
    public ResponseEntity<ApiResponse<FoodPassportResponse>> verifyPassport(@Valid @RequestBody VerifyPassportRequest request) {
        User verifier = SecurityUtils.getCurrentUser();
        FoodPassportResponse verified = passportService.verifyPassport(request, verifier);
        return ResponseEntity.ok(ApiResponse.success("Food Passport verified! Pickup handover recorded.", verified));
    }
}

