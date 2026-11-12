package com.annadhata.dto;

import com.annadhata.enums.ClaimStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.VolunteerAssignmentStatus;

import java.time.Instant;

public class VolunteerTaskResponse {
    private String claimId;
    private String donationId;
    private String foodName;
    private FoodCategory foodCategory;
    private Double claimedQuantity;
    private String measurementUnit;
    private ClaimStatus claimStatus;
    private VolunteerAssignmentStatus volunteerAssignmentStatus;
    private boolean volunteerRequested;

    private String donorId;
    private String donorName;
    private String donorPhone;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String receiverId;
    private String receiverName;
    private String receiverPhone;
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    private Double distanceFromVolunteerKm;
    private Double routeDistanceKm;
    private Instant pickupDeadline;
    private Instant confirmedPickupTime;

    private String passportCode;
    private boolean qrVerified;

    private String assignedVolunteerId;
    private String assignedVolunteerName;
    private String assignedVolunteerPhone;

    public VolunteerTaskResponse() {}

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Double getClaimedQuantity() { return claimedQuantity; }
    public void setClaimedQuantity(Double claimedQuantity) { this.claimedQuantity = claimedQuantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public ClaimStatus getClaimStatus() { return claimStatus; }
    public void setClaimStatus(ClaimStatus claimStatus) { this.claimStatus = claimStatus; }

    public VolunteerAssignmentStatus getVolunteerAssignmentStatus() { return volunteerAssignmentStatus; }
    public void setVolunteerAssignmentStatus(VolunteerAssignmentStatus volunteerAssignmentStatus) { this.volunteerAssignmentStatus = volunteerAssignmentStatus; }

    public boolean isVolunteerRequested() { return volunteerRequested; }
    public void setVolunteerRequested(boolean volunteerRequested) { this.volunteerRequested = volunteerRequested; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public Double getDropoffLatitude() { return dropoffLatitude; }
    public void setDropoffLatitude(Double dropoffLatitude) { this.dropoffLatitude = dropoffLatitude; }

    public Double getDropoffLongitude() { return dropoffLongitude; }
    public void setDropoffLongitude(Double dropoffLongitude) { this.dropoffLongitude = dropoffLongitude; }

    public Double getDistanceFromVolunteerKm() { return distanceFromVolunteerKm; }
    public void setDistanceFromVolunteerKm(Double distanceFromVolunteerKm) { this.distanceFromVolunteerKm = distanceFromVolunteerKm; }

    public Double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(Double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public Instant getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(Instant pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public Instant getConfirmedPickupTime() { return confirmedPickupTime; }
    public void setConfirmedPickupTime(Instant confirmedPickupTime) { this.confirmedPickupTime = confirmedPickupTime; }

    public String getPassportCode() { return passportCode; }
    public void setPassportCode(String passportCode) { this.passportCode = passportCode; }

    public boolean isQrVerified() { return qrVerified; }
    public void setQrVerified(boolean qrVerified) { this.qrVerified = qrVerified; }

    public String getAssignedVolunteerId() { return assignedVolunteerId; }
    public void setAssignedVolunteerId(String assignedVolunteerId) { this.assignedVolunteerId = assignedVolunteerId; }

    public String getAssignedVolunteerName() { return assignedVolunteerName; }
    public void setAssignedVolunteerName(String assignedVolunteerName) { this.assignedVolunteerName = assignedVolunteerName; }

    public String getAssignedVolunteerPhone() { return assignedVolunteerPhone; }
    public void setAssignedVolunteerPhone(String assignedVolunteerPhone) { this.assignedVolunteerPhone = assignedVolunteerPhone; }
}

