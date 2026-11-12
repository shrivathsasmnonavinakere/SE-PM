package com.annadhata.model;

import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.enums.VolunteerAssignmentStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "claims")
@CompoundIndex(name = "donation_receiver_status_idx", def = "{'donationId': 1, 'receiverId': 1, 'status': 1}")
public class Claim {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed
    private String donorId;

    @Indexed
    private String receiverId;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private Double claimedQuantity;
    private String measurementUnit = "kg";

    private Instant proposedPickupTime;
    private Instant confirmedPickupTime;

    @Indexed
    private ClaimStatus status = ClaimStatus.PENDING;

    private String notes;
    private String rejectionReason;

    // QR Food Passport metadata
    private String passportId;
    private String passportCode;
    private boolean qrVerified = false;
    private Instant qrVerifiedAt;

    // Rescue Relay metadata
    @Indexed
    private RescueRelayStatus rescueRelayStatus = RescueRelayStatus.NONE;
    private Instant missedPickupDetectedAt;
    private Instant releasedAt;
    private String rescueRelayNotes;

    // Volunteer Network assignment metadata
    private boolean volunteerRequested = false;
    private VolunteerAssignmentStatus volunteerAssignmentStatus = VolunteerAssignmentStatus.UNASSIGNED;
    @Indexed
    private String assignedVolunteerId;
    private String assignedVolunteerName;
    private String assignedVolunteerPhone;
    private Instant volunteerAcceptedAt;

    private Instant pickedUpAt;
    private Instant completedAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public Claim() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

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

    public Instant getVolunteerAcceptedAt() { return volunteerAcceptedAt; }
    public void setVolunteerAcceptedAt(Instant volunteerAcceptedAt) { this.volunteerAcceptedAt = volunteerAcceptedAt; }

    public Instant getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
