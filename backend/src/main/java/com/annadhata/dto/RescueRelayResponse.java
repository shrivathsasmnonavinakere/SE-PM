package com.annadhata.dto;

import com.annadhata.enums.RescueRelayStatus;

import java.time.Instant;
import java.util.List;

public class RescueRelayResponse {
    private String id;
    private String claimId;
    private String donationId;
    private String donorId;
    private String originalReceiverId;
    private String originalReceiverName;
    private String foodName;
    private Double quantityAtRisk;
    private String measurementUnit;
    private RescueRelayStatus status;
    private Instant confirmedPickupTimeSnapshot;
    private Instant pickupDeadlineSnapshot;
    private Instant detectedAt;
    private Instant donorNotifiedAt;
    private Instant resolvedAt;
    private String resolutionNotes;
    private List<MatchRecommendationResponse> alternativeReceivers;

    public RescueRelayResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getOriginalReceiverId() { return originalReceiverId; }
    public void setOriginalReceiverId(String originalReceiverId) { this.originalReceiverId = originalReceiverId; }

    public String getOriginalReceiverName() { return originalReceiverName; }
    public void setOriginalReceiverName(String originalReceiverName) { this.originalReceiverName = originalReceiverName; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Double getQuantityAtRisk() { return quantityAtRisk; }
    public void setQuantityAtRisk(Double quantityAtRisk) { this.quantityAtRisk = quantityAtRisk; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public RescueRelayStatus getStatus() { return status; }
    public void setStatus(RescueRelayStatus status) { this.status = status; }

    public Instant getConfirmedPickupTimeSnapshot() { return confirmedPickupTimeSnapshot; }
    public void setConfirmedPickupTimeSnapshot(Instant confirmedPickupTimeSnapshot) { this.confirmedPickupTimeSnapshot = confirmedPickupTimeSnapshot; }

    public Instant getPickupDeadlineSnapshot() { return pickupDeadlineSnapshot; }
    public void setPickupDeadlineSnapshot(Instant pickupDeadlineSnapshot) { this.pickupDeadlineSnapshot = pickupDeadlineSnapshot; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }

    public Instant getDonorNotifiedAt() { return donorNotifiedAt; }
    public void setDonorNotifiedAt(Instant donorNotifiedAt) { this.donorNotifiedAt = donorNotifiedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public List<MatchRecommendationResponse> getAlternativeReceivers() { return alternativeReceivers; }
    public void setAlternativeReceivers(List<MatchRecommendationResponse> alternativeReceivers) { this.alternativeReceivers = alternativeReceivers; }
}

