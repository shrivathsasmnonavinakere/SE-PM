package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.CreateNeedRequest;
import com.annadhata.dto.NeedResponse;
import com.annadhata.model.User;
import com.annadhata.service.ReceiverNeedsService;
import com.annadhata.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/needs")
@Tag(name = "Receiver Food Requirements", description = "Endpoints for NGOs and receivers to declare food demand and capacities")
public class ReceiverNeedsController {

    private final ReceiverNeedsService receiverNeedsService;

    public ReceiverNeedsController(ReceiverNeedsService receiverNeedsService) {
        this.receiverNeedsService = receiverNeedsService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Publish food demand requirements and receiving capacity")
    public ResponseEntity<ApiResponse<NeedResponse>> createNeed(@Valid @RequestBody CreateNeedRequest request) {
        User receiver = SecurityUtils.getCurrentUser();
        NeedResponse response = receiverNeedsService.createNeed(request, receiver);
        return ResponseEntity.ok(ApiResponse.success("Food requirements saved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Get active food requirements for the authenticated receiver")
    public ResponseEntity<ApiResponse<List<NeedResponse>>> getMyNeeds() {
        User receiver = SecurityUtils.getCurrentUser();
        List<NeedResponse> list = receiverNeedsService.getReceiverNeeds(receiver.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active food requirements across the ecosystem")
    public ResponseEntity<ApiResponse<List<NeedResponse>>> getAllActiveNeeds() {
        List<NeedResponse> list = receiverNeedsService.getAllActiveNeeds();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEIVER', 'ROLE_ADMIN')")
    @Operation(summary = "Deactivate a food requirement")
    public ResponseEntity<ApiResponse<Void>> deleteNeed(@PathVariable String id) {
        User receiver = SecurityUtils.getCurrentUser();
        receiverNeedsService.deactivateNeed(id, receiver);
        return ResponseEntity.ok(ApiResponse.success("Requirement deactivated", null));
    }
}
