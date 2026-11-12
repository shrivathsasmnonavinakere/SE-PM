package com.annadhata.dto;

import com.annadhata.enums.FoodCategory;

import java.time.Instant;
import java.time.LocalDate;

public class NeedResponse {
    private String id;
    private String receiverId;
    private String receiverName;
    private FoodCategory requiredCategory;
    private Double requiredQuantity;
    private String measurementUnit;
    private String preferredFoodType;
    private Integer numberOfBeneficiaries;
    private Double maxReceivingCapacity;
    private LocalDate requiredDate;
    private boolean pickupAvailable;
    private String pickupWindowStart;
    private String pickupWindowEnd;
    private Double latitude;
    private Double longitude;
    private String address;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public NeedResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public FoodCategory getRequiredCategory() { return requiredCategory; }
    public void setRequiredCategory(FoodCategory requiredCategory) { this.requiredCategory = requiredCategory; }

    public Double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Double requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public String getPreferredFoodType() { return preferredFoodType; }
    public void setPreferredFoodType(String preferredFoodType) { this.preferredFoodType = preferredFoodType; }

    public Integer getNumberOfBeneficiaries() { return numberOfBeneficiaries; }
    public void setNumberOfBeneficiaries(Integer numberOfBeneficiaries) { this.numberOfBeneficiaries = numberOfBeneficiaries; }

    public Double getMaxReceivingCapacity() { return maxReceivingCapacity; }
    public void setMaxReceivingCapacity(Double maxReceivingCapacity) { this.maxReceivingCapacity = maxReceivingCapacity; }

    public LocalDate getRequiredDate() { return requiredDate; }
    public void setRequiredDate(LocalDate requiredDate) { this.requiredDate = requiredDate; }

    public boolean isPickupAvailable() { return pickupAvailable; }
    public void setPickupAvailable(boolean pickupAvailable) { this.pickupAvailable = pickupAvailable; }

    public String getPickupWindowStart() { return pickupWindowStart; }
    public void setPickupWindowStart(String pickupWindowStart) { this.pickupWindowStart = pickupWindowStart; }

    public String getPickupWindowEnd() { return pickupWindowEnd; }
    public void setPickupWindowEnd(String pickupWindowEnd) { this.pickupWindowEnd = pickupWindowEnd; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
