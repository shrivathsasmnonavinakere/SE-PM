package com.annadhata.dto;

import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;

import java.time.Instant;

public class UpdateDonationRequest {
    private String foodName;
    private String foodDescription;
    private FoodCategory foodCategory;
    private Double quantity;
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

    public UpdateDonationRequest() {}

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
}
