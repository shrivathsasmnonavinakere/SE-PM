package com.annadhata.controller;

import com.annadhata.config.DataInitializer;
import com.annadhata.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Utilities", description = "Endpoints for seeding demo data and health testing")
public class DataSeedController {

    private final DataInitializer dataInitializer;

    public DataSeedController(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed sample restaurants, NGOs, surplus biryani, needs, and impact records")
    public ResponseEntity<ApiResponse<String>> seedData() {
        dataInitializer.seedInitialDataIfEmpty();
        return ResponseEntity.ok(ApiResponse.success("Demo dataset seeded successfully", "Ready for live demonstration"));
    }
}
