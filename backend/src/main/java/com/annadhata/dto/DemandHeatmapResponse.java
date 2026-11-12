package com.annadhata.dto;

import java.util.List;
import java.util.Map;

public class DemandHeatmapResponse {

    private String timeRange;
    private String categoryFilter;
    private double totalHistoricalSurplusKg;
    private double totalActiveDemandKg;
    private double totalFulfilledKg;
    private double totalExpiredKg;
    private String peakSurplusHours;
    private String highestDemandZone;
    private String highestSurplusZone;
    private String aiSpatialInsight;
    private List<HeatmapZone> zones;
    private Map<String, Double> demandVsSurplusByCategory;

    public DemandHeatmapResponse() {}

    public static class HeatmapZone {
        private String zoneName;
        private double centerLat;
        private double centerLon;
        private double totalSurplusKg;
        private double totalDemandKg;
        private double fulfilledKg;
        private double expiredKg;
        private double deficitKg; // positive means demand exceeds surplus
        private double surplusIntensity; // 0..1
        private double demandIntensity;  // 0..1
        private double gapIntensity;     // 0..1
        private int donationCount;
        private int needCount;
        private String topSurplusCategory;
        private String topDemandCategory;
        private String recommendedAction;

        public HeatmapZone() {}

        public String getZoneName() { return zoneName; }
        public void setZoneName(String zoneName) { this.zoneName = zoneName; }

        public double getCenterLat() { return centerLat; }
        public void setCenterLat(double centerLat) { this.centerLat = centerLat; }

        public double getCenterLon() { return centerLon; }
        public void setCenterLon(double centerLon) { this.centerLon = centerLon; }

        public double getTotalSurplusKg() { return totalSurplusKg; }
        public void setTotalSurplusKg(double totalSurplusKg) { this.totalSurplusKg = totalSurplusKg; }

        public double getTotalDemandKg() { return totalDemandKg; }
        public void setTotalDemandKg(double totalDemandKg) { this.totalDemandKg = totalDemandKg; }

        public double getFulfilledKg() { return fulfilledKg; }
        public void setFulfilledKg(double fulfilledKg) { this.fulfilledKg = fulfilledKg; }

        public double getExpiredKg() { return expiredKg; }
        public void setExpiredKg(double expiredKg) { this.expiredKg = expiredKg; }

        public double getDeficitKg() { return deficitKg; }
        public void setDeficitKg(double deficitKg) { this.deficitKg = deficitKg; }

        public double getSurplusIntensity() { return surplusIntensity; }
        public void setSurplusIntensity(double surplusIntensity) { this.surplusIntensity = surplusIntensity; }

        public double getDemandIntensity() { return demandIntensity; }
        public void setDemandIntensity(double demandIntensity) { this.demandIntensity = demandIntensity; }

        public double getGapIntensity() { return gapIntensity; }
        public void setGapIntensity(double gapIntensity) { this.gapIntensity = gapIntensity; }

        public int getDonationCount() { return donationCount; }
        public void setDonationCount(int donationCount) { this.donationCount = donationCount; }

        public int getNeedCount() { return needCount; }
        public void setNeedCount(int needCount) { this.needCount = needCount; }

        public String getTopSurplusCategory() { return topSurplusCategory; }
        public void setTopSurplusCategory(String topSurplusCategory) { this.topSurplusCategory = topSurplusCategory; }

        public String getTopDemandCategory() { return topDemandCategory; }
        public void setTopDemandCategory(String topDemandCategory) { this.topDemandCategory = topDemandCategory; }

        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getCategoryFilter() { return categoryFilter; }
    public void setCategoryFilter(String categoryFilter) { this.categoryFilter = categoryFilter; }

    public double getTotalHistoricalSurplusKg() { return totalHistoricalSurplusKg; }
    public void setTotalHistoricalSurplusKg(double totalHistoricalSurplusKg) { this.totalHistoricalSurplusKg = totalHistoricalSurplusKg; }

    public double getTotalActiveDemandKg() { return totalActiveDemandKg; }
    public void setTotalActiveDemandKg(double totalActiveDemandKg) { this.totalActiveDemandKg = totalActiveDemandKg; }

    public double getTotalFulfilledKg() { return totalFulfilledKg; }
    public void setTotalFulfilledKg(double totalFulfilledKg) { this.totalFulfilledKg = totalFulfilledKg; }

    public double getTotalExpiredKg() { return totalExpiredKg; }
    public void setTotalExpiredKg(double totalExpiredKg) { this.totalExpiredKg = totalExpiredKg; }

    public String getPeakSurplusHours() { return peakSurplusHours; }
    public void setPeakSurplusHours(String peakSurplusHours) { this.peakSurplusHours = peakSurplusHours; }

    public String getHighestDemandZone() { return highestDemandZone; }
    public void setHighestDemandZone(String highestDemandZone) { this.highestDemandZone = highestDemandZone; }

    public String getHighestSurplusZone() { return highestSurplusZone; }
    public void setHighestSurplusZone(String highestSurplusZone) { this.highestSurplusZone = highestSurplusZone; }

    public String getAiSpatialInsight() { return aiSpatialInsight; }
    public void setAiSpatialInsight(String aiSpatialInsight) { this.aiSpatialInsight = aiSpatialInsight; }

    public List<HeatmapZone> getZones() { return zones; }
    public void setZones(List<HeatmapZone> zones) { this.zones = zones; }

    public Map<String, Double> getDemandVsSurplusByCategory() { return demandVsSurplusByCategory; }
    public void setDemandVsSurplusByCategory(Map<String, Double> demandVsSurplusByCategory) { this.demandVsSurplusByCategory = demandVsSurplusByCategory; }
}

