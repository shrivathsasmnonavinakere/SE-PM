package com.annadhata.dto;

import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.SafetyIndicator;

import java.time.Instant;

public class DonationResponse {
    private String id;
    private String donorId;
    private String donorName;
    private String donorPhone;
    private String foodName;
    private String foodDescription;
    private FoodCategory foodCategory;
    private Double quantity;
    private Double remainingQuantity;
    private Double allocatedQuantity;
    private Double allocationPercentage;
    private Integer allocationCount;
    private String measurementUnit;
    private String foodImage;
    private Instant preparationDateTime;
    private Instant pickupDeadline;
    private String pickupAddress;
    private Double latitude;
    private Double longitude;
    private String foodHandlingDetails;
    private String storageInformation;
    private DonationStatus status;
    private SafetyIndicator safetyIndicator;
    private String safetyNotes;
    private String claimedByReceiverId;
    private String claimedByReceiverName;
    private Double distanceKm; // Computed dynamically for the requesting receiver
    private Double donorTrustScore;
    private String donorTrustTier;
    private Instant createdAt;
    private Instant updatedAt;

    public DonationResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public String getFoodImage() { return foodImage; }
    public void setFoodImage(String foodImage) { this.foodImage = foodImage; }

    public Instant getPreparationDateTime() { return preparationDateTime; }
    public void setPreparationDateTime(Instant preparationDateTime) { this.preparationDateTime = preparationDateTime; }

    public Instant getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(Instant pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getFoodHandlingDetails() { return foodHandlingDetails; }
    public void setFoodHandlingDetails(String foodHandlingDetails) { this.foodHandlingDetails = foodHandlingDetails; }

    public String getStorageInformation() { return storageInformation; }
    public void setStorageInformation(String storageInformation) { this.storageInformation = storageInformation; }

    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }

    public SafetyIndicator getSafetyIndicator() { return safetyIndicator; }
    public void setSafetyIndicator(SafetyIndicator safetyIndicator) { this.safetyIndicator = safetyIndicator; }

    public String getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(String safetyNotes) { this.safetyNotes = safetyNotes; }

    public String getClaimedByReceiverId() { return claimedByReceiverId; }
    public void setClaimedByReceiverId(String claimedByReceiverId) { this.claimedByReceiverId = claimedByReceiverId; }

    public String getClaimedByReceiverName() { return claimedByReceiverName; }
    public void setClaimedByReceiverName(String claimedByReceiverName) { this.claimedByReceiverName = claimedByReceiverName; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public Double getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Double allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public Double getAllocationPercentage() { return allocationPercentage; }
    public void setAllocationPercentage(Double allocationPercentage) { this.allocationPercentage = allocationPercentage; }

    public Integer getAllocationCount() { return allocationCount; }
    public void setAllocationCount(Integer allocationCount) { this.allocationCount = allocationCount; }

    public Double getDonorTrustScore() { return donorTrustScore; }
    public void setDonorTrustScore(Double donorTrustScore) { this.donorTrustScore = donorTrustScore; }

    public String getDonorTrustTier() { return donorTrustTier; }
    public void setDonorTrustTier(String donorTrustTier) { this.donorTrustTier = donorTrustTier; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
