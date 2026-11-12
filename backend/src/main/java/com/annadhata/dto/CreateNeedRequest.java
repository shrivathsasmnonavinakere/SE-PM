package com.annadhata.dto;

import com.annadhata.enums.FoodCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateNeedRequest {

    @NotNull(message = "Category is required")
    private FoodCategory requiredCategory;

    @NotNull(message = "Required quantity is required")
    @DecimalMin(value = "0.1", message = "Quantity must be greater than 0")
    private Double requiredQuantity;

    private String measurementUnit = "kg";
    private String preferredFoodType;
    private Integer numberOfBeneficiaries;
    private Double maxReceivingCapacity;
    private LocalDate requiredDate;
    private boolean pickupAvailable = true;
    private String pickupWindowStart;
    private String pickupWindowEnd;
    private Double latitude;
    private Double longitude;
    private String address;

    public CreateNeedRequest() {}

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
}
