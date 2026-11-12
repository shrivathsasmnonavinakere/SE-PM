package com.annadhata.dto;

import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.VolunteerAssignmentStatus;

import java.time.Instant;

public class ClaimResponse {
    private String id;
    private String donationId;
    private String donorId;
    private String donorName;
    private String donorPhone;
    private String pickupAddress;
    private Double donorLatitude;
    private Double donorLongitude;
    private Instant pickupDeadline;
    private String receiverId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String foodName;
    private Double claimedQuantity;
    private String measurementUnit;
    private Instant proposedPickupTime;
    private Instant confirmedPickupTime;
    private ClaimStatus status;
    private String notes;
    private String rejectionReason;

    // QR Food Passport fields
    private String passportId;
    private String passportCode;
    private boolean qrVerified;
    private Instant qrVerifiedAt;

    // Rescue Relay fields
    private RescueRelayStatus rescueRelayStatus;
    private Instant missedPickupDetectedAt;
    private Instant releasedAt;
    private String rescueRelayNotes;

    // Volunteer Network fields
    private boolean volunteerRequested;
    private VolunteerAssignmentStatus volunteerAssignmentStatus;
    private String assignedVolunteerId;
    private String assignedVolunteerName;
    private String assignedVolunteerPhone;

    // Trust fields
    private Double receiverTrustScore;
    private String receiverTrustTier;

    private Instant pickedUpAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ClaimResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Double getDonorLatitude() { return donorLatitude; }
    public void setDonorLatitude(Double donorLatitude) { this.donorLatitude = donorLatitude; }

    public Double getDonorLongitude() { return donorLongitude; }
    public void setDonorLongitude(Double donorLongitude) { this.donorLongitude = donorLongitude; }

    public Instant getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(Instant pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Double getClaimedQuantity() { return claimedQuantity; }
    public void setClaimedQuantity(Double claimedQuantity) { this.claimedQuantity = claimedQuantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public Instant getProposedPickupTime() { return proposedPickupTime; }
    public void setProposedPickupTime(Instant proposedPickupTime) { this.proposedPickupTime = proposedPickupTime; }

    public Instant getConfirmedPickupTime() { return confirmedPickupTime; }
    public void setConfirmedPickupTime(Instant confirmedPickupTime) { this.confirmedPickupTime = confirmedPickupTime; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getPassportId() { return passportId; }
    public void setPassportId(String passportId) { this.passportId = passportId; }

    public String getPassportCode() { return passportCode; }
    public void setPassportCode(String passportCode) { this.passportCode = passportCode; }

    public boolean isQrVerified() { return qrVerified; }
    public void setQrVerified(boolean qrVerified) { this.qrVerified = qrVerified; }

    public Instant getQrVerifiedAt() { return qrVerifiedAt; }
    public void setQrVerifiedAt(Instant qrVerifiedAt) { this.qrVerifiedAt = qrVerifiedAt; }

    public RescueRelayStatus getRescueRelayStatus() { return rescueRelayStatus; }
    public void setRescueRelayStatus(RescueRelayStatus rescueRelayStatus) { this.rescueRelayStatus = rescueRelayStatus; }

    public Instant getMissedPickupDetectedAt() { return missedPickupDetectedAt; }
    public void setMissedPickupDetectedAt(Instant missedPickupDetectedAt) { this.missedPickupDetectedAt = missedPickupDetectedAt; }

    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    public String getRescueRelayNotes() { return rescueRelayNotes; }
    public void setRescueRelayNotes(String rescueRelayNotes) { this.rescueRelayNotes = rescueRelayNotes; }

    public boolean isVolunteerRequested() { return volunteerRequested; }
    public void setVolunteerRequested(boolean volunteerRequested) { this.volunteerRequested = volunteerRequested; }

    public VolunteerAssignmentStatus getVolunteerAssignmentStatus() { return volunteerAssignmentStatus; }
    public void setVolunteerAssignmentStatus(VolunteerAssignmentStatus volunteerAssignmentStatus) { this.volunteerAssignmentStatus = volunteerAssignmentStatus; }

    public String getAssignedVolunteerId() { return assignedVolunteerId; }
    public void setAssignedVolunteerId(String assignedVolunteerId) { this.assignedVolunteerId = assignedVolunteerId; }

    public String getAssignedVolunteerName() { return assignedVolunteerName; }
    public void setAssignedVolunteerName(String assignedVolunteerName) { this.assignedVolunteerName = assignedVolunteerName; }

    public String getAssignedVolunteerPhone() { return assignedVolunteerPhone; }
    public void setAssignedVolunteerPhone(String assignedVolunteerPhone) { this.assignedVolunteerPhone = assignedVolunteerPhone; }

    public Double getReceiverTrustScore() { return receiverTrustScore; }
    public void setReceiverTrustScore(Double receiverTrustScore) { this.receiverTrustScore = receiverTrustScore; }

    public String getReceiverTrustTier() { return receiverTrustTier; }
    public void setReceiverTrustTier(String receiverTrustTier) { this.receiverTrustTier = receiverTrustTier; }

    public Instant getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
