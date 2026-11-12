package com.annadhata.model;

import com.annadhata.enums.FoodCategory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "receiverNeeds")
public class ReceiverNeed {

    @Id
    private String id;

    @Indexed
    private String receiverId;

    private String receiverName;

    @Indexed
    private FoodCategory requiredCategory;

    private Double requiredQuantity;
    private String measurementUnit = "kg";

    private String preferredFoodType;
    private Integer numberOfBeneficiaries;
    private Double maxReceivingCapacity;

    private LocalDate requiredDate;
    private boolean pickupAvailable = true;
    private String pickupWindowStart; // e.g. "09:00"
    private String pickupWindowEnd;   // e.g. "20:00"

    private Double latitude;
    private Double longitude;
    private String address;

    private boolean active = true;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public ReceiverNeed() {}

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
