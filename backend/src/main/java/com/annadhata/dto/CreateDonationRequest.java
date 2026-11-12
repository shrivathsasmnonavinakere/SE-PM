package com.annadhata.dto;

import com.annadhata.enums.FoodCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateDonationRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    private String foodDescription;

    @NotNull(message = "Food category is required")
    private FoodCategory foodCategory;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.1", message = "Quantity must be greater than 0")
    private Double quantity;

    private String measurementUnit = "kg";

    private String foodImage;

    @NotNull(message = "Preparation time is required")
    private Instant preparationDateTime;

    @NotNull(message = "Pickup deadline is required")
    private Instant pickupDeadline;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    private Double latitude;
    private Double longitude;

    private String foodHandlingDetails;
    private String storageInformation;

    public CreateDonationRequest() {}

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
}
