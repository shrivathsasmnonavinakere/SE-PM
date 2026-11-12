package com.annadhata;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.DemandHeatmapResponse;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.model.Donation;
import com.annadhata.model.ImpactRecord;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.ImpactRecordRepository;
import com.annadhata.repository.ReceiverNeedRepository;
import com.annadhata.service.HeatmapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeatmapServiceTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private ReceiverNeedRepository receiverNeedRepository;
    @Mock
    private ImpactRecordRepository impactRecordRepository;

    private HeatmapService heatmapService;

    @BeforeEach
    void setUp() {
        heatmapService = new HeatmapService(
                donationRepository, receiverNeedRepository, impactRecordRepository, new GeminiAIService()
        );
    }

    @Test
    @DisplayName("AI Demand Heatmap aggregates real historical surplus, active demand, and expired bottlenecks across spatial zones")
    void testHeatmapClustersAndAggregatesZones() {
        Donation don1 = new Donation();
        don1.setId("d1");
        don1.setFoodCategory(FoodCategory.COOKED_FOOD);
        don1.setQuantity(35.0);
        don1.setRemainingQuantity(35.0);
        don1.setStatus(DonationStatus.COMPLETED);
        don1.setLatitude(12.9716);
        don1.setLongitude(77.5946);
        don1.setPickupAddress("12, Brigade Road, Bangalore");
        don1.setCreatedAt(Instant.now());

        Donation don2 = new Donation();
        don2.setId("d2");
        don2.setFoodCategory(FoodCategory.FRUITS);
        don2.setQuantity(12.0);
        don2.setRemainingQuantity(12.0);
        don2.setStatus(DonationStatus.EXPIRED);
        don2.setLatitude(12.9698);
        don2.setLongitude(77.7499); // Whitefield (~17 km away -> separate cluster)
        don2.setPickupAddress("ITPL Road, Whitefield, Bangalore");
        don2.setCreatedAt(Instant.now());

        ReceiverNeed need = new ReceiverNeed();
        need.setId("n1");
        need.setRequiredCategory(FoodCategory.COOKED_FOOD);
        need.setRequiredQuantity(60.0);
        need.setLatitude(12.9720);
        need.setLongitude(77.5950); // Same Brigade Rd cluster
        need.setAddress("Richmond Town, Brigade Road, Bangalore");
        need.setActive(true);

        ImpactRecord impact = new ImpactRecord("d1", "donor1", "rec1", FoodCategory.COOKED_FOOD, 35.0, "Brigade Road");
        impact.setCreatedAt(Instant.now());

        when(donationRepository.findAll()).thenReturn(List.of(don1, don2));
        when(receiverNeedRepository.findByActiveTrue()).thenReturn(List.of(need));
        when(impactRecordRepository.findAll()).thenReturn(List.of(impact));

        DemandHeatmapResponse resp = heatmapService.generateHeatmap("all", null);

        assertEquals(47.0, resp.getTotalHistoricalSurplusKg());
        assertEquals(60.0, resp.getTotalActiveDemandKg());
        assertEquals(35.0, resp.getTotalFulfilledKg());
        assertEquals(12.0, resp.getTotalExpiredKg());
        assertEquals(2, resp.getZones().size());
        assertNotNull(resp.getAiSpatialInsight());

        // Filter by FRUITS only should leave 1 zone (Whitefield)
        DemandHeatmapResponse fruitOnly = heatmapService.generateHeatmap("all", FoodCategory.FRUITS);
        assertEquals(12.0, fruitOnly.getTotalHistoricalSurplusKg());
        assertEquals(1, fruitOnly.getZones().size());
    }
}

