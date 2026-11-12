package com.annadhata.service;

import com.annadhata.ai.GeminiAIService;
import com.annadhata.dto.DemandHeatmapResponse;
import com.annadhata.dto.DemandHeatmapResponse.HeatmapZone;
import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.model.Donation;
import com.annadhata.model.ImpactRecord;
import com.annadhata.model.ReceiverNeed;
import com.annadhata.repository.DonationRepository;
import com.annadhata.repository.ImpactRecordRepository;
import com.annadhata.repository.ReceiverNeedRepository;
import com.annadhata.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class HeatmapService {

    private static final double CLUSTER_RADIUS_KM = 2.2;

    private final DonationRepository donationRepository;
    private final ReceiverNeedRepository receiverNeedRepository;
    private final ImpactRecordRepository impactRecordRepository;
    private final GeminiAIService geminiAIService;

    public HeatmapService(DonationRepository donationRepository,
                          ReceiverNeedRepository receiverNeedRepository,
                          ImpactRecordRepository impactRecordRepository,
                          GeminiAIService geminiAIService) {
        this.donationRepository = donationRepository;
        this.receiverNeedRepository = receiverNeedRepository;
        this.impactRecordRepository = impactRecordRepository;
        this.geminiAIService = geminiAIService;
    }

    public DemandHeatmapResponse generateHeatmap(String timeRange, FoodCategory categoryFilter) {
        Instant cutoff = resolveCutoff(timeRange);

        List<Donation> donations = donationRepository.findAll().stream()
                .filter(d -> d.getStatus() != DonationStatus.CANCELLED)
                .filter(d -> cutoff == null || (d.getCreatedAt() != null && !d.getCreatedAt().isBefore(cutoff)))
                .filter(d -> categoryFilter == null || d.getFoodCategory() == categoryFilter)
                .toList();

        List<ReceiverNeed> needs = receiverNeedRepository.findByActiveTrue().stream()
                .filter(n -> categoryFilter == null || n.getRequiredCategory() == categoryFilter)
                .toList();

        List<ImpactRecord> impacts = impactRecordRepository.findAll().stream()
                .filter(i -> cutoff == null || (i.getCreatedAt() != null && !i.getCreatedAt().isBefore(cutoff)))
                .filter(i -> categoryFilter == null || i.getFoodCategory() == categoryFilter)
                .toList();

        Map<String, Donation> donationById = new HashMap<>();
        for (Donation d : donations) {
            donationById.put(d.getId(), d);
        }

        List<ZoneAccumulator> accumulators = new ArrayList<>();

        // Cluster historical & active donations
        for (Donation d : donations) {
            if (d.getLatitude() == null || d.getLongitude() == null) continue;
            ZoneAccumulator zone = findOrCreateCluster(accumulators, d.getLatitude(), d.getLongitude(), extractZoneLabel(d.getPickupAddress()));
            double qty = d.getQuantity() != null ? d.getQuantity() : 0.0;
            zone.surplusKg += qty;
            zone.donationCount += 1;
            if (d.getStatus() == DonationStatus.EXPIRED) {
                zone.expiredKg += (d.getRemainingQuantity() != null ? d.getRemainingQuantity() : qty);
            }
            if (d.getFoodCategory() != null) {
                zone.surplusCategories.merge(d.getFoodCategory().getDisplayName(), qty, Double::sum);
            }
        }

        // Cluster active receiver demands into same spatial clusters
        for (ReceiverNeed n : needs) {
            if (n.getLatitude() == null || n.getLongitude() == null) continue;
            ZoneAccumulator zone = findOrCreateCluster(accumulators, n.getLatitude(), n.getLongitude(), extractZoneLabel(n.getAddress()));
            double reqQty = n.getRequiredQuantity() != null ? n.getRequiredQuantity() : 0.0;
            zone.demandKg += reqQty;
            zone.needCount += 1;
            if (n.getRequiredCategory() != null) {
                zone.demandCategories.merge(n.getRequiredCategory().getDisplayName(), reqQty, Double::sum);
            }
        }

        // Attribute historical fulfilled quantities cleanly
        for (ImpactRecord imp : impacts) {
            Donation d = imp.getDonationId() != null ? donationById.get(imp.getDonationId()) : null;
            if (d != null && d.getLatitude() != null && d.getLongitude() != null) {
                ZoneAccumulator zone = findOrCreateCluster(accumulators, d.getLatitude(), d.getLongitude(), extractZoneLabel(d.getPickupAddress()));
                zone.fulfilledKg += imp.getQuantityKg() != null ? imp.getQuantityKg() : 0.0;
            }
        }

        // Normalize intensities across zones
        double maxSurplus = accumulators.stream().mapToDouble(z -> z.surplusKg).max().orElse(1.0);
        double maxDemand = accumulators.stream().mapToDouble(z -> z.demandKg).max().orElse(1.0);
        double maxDeficit = accumulators.stream().mapToDouble(z -> Math.max(0.0, z.demandKg - z.surplusKg)).max().orElse(1.0);

        List<HeatmapZone> zones = new ArrayList<>();
        for (ZoneAccumulator acc : accumulators) {
            HeatmapZone hz = new HeatmapZone();
            hz.setZoneName(acc.zoneName);
            hz.setCenterLat(round4(acc.latSum / acc.pointCount));
            hz.setCenterLon(round4(acc.lonSum / acc.pointCount));
            hz.setTotalSurplusKg(round1(acc.surplusKg));
            hz.setTotalDemandKg(round1(acc.demandKg));
            hz.setFulfilledKg(round1(acc.fulfilledKg));
            hz.setExpiredKg(round1(acc.expiredKg));
            double deficit = Math.max(0.0, acc.demandKg - acc.surplusKg);
            hz.setDeficitKg(round1(deficit));
            hz.setSurplusIntensity(maxSurplus > 0 ? round2(acc.surplusKg / maxSurplus) : 0.0);
            hz.setDemandIntensity(maxDemand > 0 ? round2(acc.demandKg / maxDemand) : 0.0);
            hz.setGapIntensity(maxDeficit > 0 ? round2(deficit / maxDeficit) : 0.0);
            hz.setDonationCount(acc.donationCount);
            hz.setNeedCount(acc.needCount);
            hz.setTopSurplusCategory(acc.surplusCategories.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A"));
            hz.setTopDemandCategory(acc.demandCategories.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A"));

            if (deficit > 15.0) {
                hz.setRecommendedAction("High unmet demand: prioritize volunteer inter-zone delivery into " + acc.zoneName);
            } else if (acc.expiredKg > 0) {
                hz.setRecommendedAction("Expiration alert: deploy faster partial claiming or Rescue Relay in " + acc.zoneName);
            } else if (acc.surplusKg > acc.demandKg) {
                hz.setRecommendedAction("Surplus hub: route excess batches outward to nearby deficit shelters");
            } else {
                hz.setRecommendedAction("Balanced zone: maintain real-time pickup monitoring");
            }
            zones.add(hz);
        }

        zones.sort((a, b) -> Double.compare(b.getTotalSurplusKg() + b.getTotalDemandKg(), a.getTotalSurplusKg() + a.getTotalDemandKg()));

        // Peak hour computation across real donations
        Map<Integer, Long> hourBuckets = new HashMap<>();
        ZoneId tz = ZoneId.systemDefault();
        for (Donation d : donations) {
            Instant ts = d.getPreparationDateTime() != null ? d.getPreparationDateTime() : d.getCreatedAt();
            if (ts != null) {
                int startHour = (ZonedDateTime.ofInstant(ts, tz).getHour() / 3) * 3;
                hourBuckets.merge(startHour, 1L, Long::sum);
            }
        }
        String peakHours = hourBuckets.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00 - %02d:00", e.getKey(), (e.getKey() + 3) % 24))
                .orElse("No donation window recorded");

        // Demand vs Surplus by category map
        Map<String, Double> netBalanceByCat = new LinkedHashMap<>();
        for (FoodCategory fc : FoodCategory.values()) {
            double sup = donations.stream().filter(d -> d.getFoodCategory() == fc)
                    .mapToDouble(d -> d.getQuantity() != null ? d.getQuantity() : 0.0).sum();
            double dem = needs.stream().filter(n -> n.getRequiredCategory() == fc)
                    .mapToDouble(n -> n.getRequiredQuantity() != null ? n.getRequiredQuantity() : 0.0).sum();
            if (sup > 0 || dem > 0) {
                netBalanceByCat.put(fc.getDisplayName(), round1(sup - dem));
            }
        }

        DemandHeatmapResponse resp = new DemandHeatmapResponse();
        resp.setTimeRange(timeRange != null ? timeRange : "all");
        resp.setCategoryFilter(categoryFilter != null ? categoryFilter.getDisplayName() : "ALL");
        resp.setTotalHistoricalSurplusKg(round1(donations.stream().mapToDouble(d -> d.getQuantity() != null ? d.getQuantity() : 0.0).sum()));
        resp.setTotalActiveDemandKg(round1(needs.stream().mapToDouble(n -> n.getRequiredQuantity() != null ? n.getRequiredQuantity() : 0.0).sum()));
        resp.setTotalFulfilledKg(round1(impacts.stream().mapToDouble(i -> i.getQuantityKg() != null ? i.getQuantityKg() : 0.0).sum()));
        resp.setTotalExpiredKg(round1(zones.stream().mapToDouble(HeatmapZone::getExpiredKg).sum()));
        resp.setPeakSurplusHours(peakHours);
        resp.setHighestSurplusZone(zones.stream().max(Comparator.comparingDouble(HeatmapZone::getTotalSurplusKg)).map(HeatmapZone::getZoneName).orElse("N/A"));
        resp.setHighestDemandZone(zones.stream().max(Comparator.comparingDouble(HeatmapZone::getTotalDemandKg)).map(HeatmapZone::getZoneName).orElse("N/A"));
        resp.setZones(zones);
        resp.setDemandVsSurplusByCategory(netBalanceByCat);
        resp.setAiSpatialInsight(geminiAIService.generateHeatmapSpatialInsight(resp));

        return resp;
    }

    private Instant resolveCutoff(String timeRange) {
        if ("7d".equalsIgnoreCase(timeRange)) return Instant.now().minus(7, ChronoUnit.DAYS);
        if ("30d".equalsIgnoreCase(timeRange)) return Instant.now().minus(30, ChronoUnit.DAYS);
        return null;
    }

    private ZoneAccumulator findOrCreateCluster(List<ZoneAccumulator> accumulators, double lat, double lon, String label) {
        for (ZoneAccumulator acc : accumulators) {
            double cLat = acc.latSum / acc.pointCount;
            double cLon = acc.lonSum / acc.pointCount;
            if (GeoUtils.haversineDistance(lat, lon, cLat, cLon) <= CLUSTER_RADIUS_KM) {
                acc.latSum += lat;
                acc.lonSum += lon;
                acc.pointCount++;
                return acc;
            }
        }
        ZoneAccumulator created = new ZoneAccumulator(label, lat, lon);
        accumulators.add(created);
        return created;
    }

    private String extractZoneLabel(String address) {
        if (address == null || address.isBlank()) return "Urban Zone";
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            String candidate = parts[1].trim();
            return candidate.length() > 28 ? candidate.substring(0, 28) : candidate;
        }
        String candidate = parts[0].trim();
        return candidate.length() > 28 ? candidate.substring(0, 28) : candidate;
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }

    private static class ZoneAccumulator {
        String zoneName;
        double latSum;
        double lonSum;
        int pointCount = 1;
        double surplusKg = 0.0;
        double demandKg = 0.0;
        double fulfilledKg = 0.0;
        double expiredKg = 0.0;
        int donationCount = 0;
        int needCount = 0;
        Map<String, Double> surplusCategories = new HashMap<>();
        Map<String, Double> demandCategories = new HashMap<>();

        ZoneAccumulator(String zoneName, double lat, double lon) {
            this.zoneName = zoneName;
            this.latSum = lat;
            this.lonSum = lon;
        }
    }
}

