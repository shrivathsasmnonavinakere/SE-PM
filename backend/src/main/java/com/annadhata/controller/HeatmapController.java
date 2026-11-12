package com.annadhata.controller;

import com.annadhata.dto.ApiResponse;
import com.annadhata.dto.DemandHeatmapResponse;
import com.annadhata.enums.FoodCategory;
import com.annadhata.service.HeatmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "AI Demand Heatmap", description = "Spatial clustering and Gemini insights of historical surplus and demand zones")
public class HeatmapController {

    private final HeatmapService heatmapService;

    public HeatmapController(HeatmapService heatmapService) {
        this.heatmapService = heatmapService;
    }

    @GetMapping("/heatmap")
    @Operation(summary = "Compute historical food demand vs surplus spatial heatmap")
    public ResponseEntity<ApiResponse<DemandHeatmapResponse>> getDemandHeatmap(
            @RequestParam(defaultValue = "all") String timeRange,
            @RequestParam(required = false) FoodCategory category) {
        DemandHeatmapResponse response = heatmapService.generateHeatmap(timeRange, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

