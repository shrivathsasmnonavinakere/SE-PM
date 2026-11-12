package com.annadhata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateClaimRequest {

    @NotBlank(message = "Donation ID is required")
    private String donationId;

    @NotNull(message = "Claimed quantity is required")
    @DecimalMin(value = "0.1", message = "Quantity must be greater than 0")
    private Double claimedQuantity;

    private String measurementUnit = "kg";

    @NotNull(message = "Proposed pickup time is required")
    private Instant proposedPickupTime;

    private String notes;
    private boolean volunteerRequested = false;

    public CreateClaimRequest() {}

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public Double getClaimedQuantity() { return claimedQuantity; }
    public void setClaimedQuantity(Double claimedQuantity) { this.claimedQuantity = claimedQuantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public Instant getProposedPickupTime() { return proposedPickupTime; }
    public void setProposedPickupTime(Instant proposedPickupTime) { this.proposedPickupTime = proposedPickupTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isVolunteerRequested() { return volunteerRequested; }
    public void setVolunteerRequested(boolean volunteerRequested) { this.volunteerRequested = volunteerRequested; }
}
