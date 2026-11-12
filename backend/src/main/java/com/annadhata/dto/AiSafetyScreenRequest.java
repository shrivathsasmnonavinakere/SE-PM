package com.annadhata.dto;

import com.annadhata.enums.FoodCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AiSafetyScreenRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    private String foodDescription;

    @NotNull(message = "Food category is required")
    private FoodCategory foodCategory = FoodCategory.COOKED_FOOD;

    private Instant preparationDateTime;
    private Instant pickupDeadline;
    private String foodHandlingDetails;
    private String storageInformation;

    public AiSafetyScreenRequest() {}

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Instant getPreparationDateTime() { return preparationDateTime; }
    public void setPreparationDateTime(Instant preparationDateTime) { this.preparationDateTime = preparationDateTime; }

    public Instant getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(Instant pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public String getFoodHandlingDetails() { return foodHandlingDetails; }
    public void setFoodHandlingDetails(String foodHandlingDetails) { this.foodHandlingDetails = foodHandlingDetails; }

    public String getStorageInformation() { return storageInformation; }
    public void setStorageInformation(String storageInformation) { this.storageInformation = storageInformation; }
}

