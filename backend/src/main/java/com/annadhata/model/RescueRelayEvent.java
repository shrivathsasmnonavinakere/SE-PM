package com.annadhata.model;

import com.annadhata.enums.RescueRelayStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "rescueRelayEvents")
public class RescueRelayEvent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String claimId;

    @Indexed
    private String donationId;

    @Indexed
    private String donorId;

    @Indexed
    private String originalReceiverId;

    private String originalReceiverName;
    private String foodName;
    private Double quantityAtRisk;
    private String measurementUnit;

    @Indexed
    private RescueRelayStatus status = RescueRelayStatus.AWAITING_DONOR_APPROVAL;

    private Instant confirmedPickupTimeSnapshot;
    private Instant pickupDeadlineSnapshot;
    private Instant detectedAt = Instant.now();
    private Instant donorNotifiedAt;
    private Instant resolvedAt;

    private String resolvedByUserId;
    private List<String> alternativeReceiverIds = new ArrayList<>();
    private String resolutionNotes;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public RescueRelayEvent() {}

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

    public String getResolvedByUserId() { return resolvedByUserId; }
    public void setResolvedByUserId(String resolvedByUserId) { this.resolvedByUserId = resolvedByUserId; }

    public List<String> getAlternativeReceiverIds() { return alternativeReceiverIds; }
    public void setAlternativeReceiverIds(List<String> alternativeReceiverIds) { this.alternativeReceiverIds = alternativeReceiverIds; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

