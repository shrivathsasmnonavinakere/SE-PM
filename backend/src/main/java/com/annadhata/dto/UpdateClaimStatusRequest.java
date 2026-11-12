package com.annadhata.dto;

import com.annadhata.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class UpdateClaimStatusRequest {

    @NotNull(message = "Status is required")
    private ClaimStatus status;

    private Instant confirmedPickupTime;
    private String rejectionReason;

    public UpdateClaimStatusRequest() {}

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public Instant getConfirmedPickupTime() { return confirmedPickupTime; }
    public void setConfirmedPickupTime(Instant confirmedPickupTime) { this.confirmedPickupTime = confirmedPickupTime; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
