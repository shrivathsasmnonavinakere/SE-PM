package com.annadhata.dto;

import com.annadhata.enums.FoodCategory;

import java.time.Instant;

public class AiExtractionResponse {
    private String foodName;
    private Double quantity;
    private String unit = "kg";
    private FoodCategory foodCategory = FoodCategory.COOKED_FOOD;
    private Instant preparationDateTime;
    private Instant pickupDeadline;
    private String foodDescription;
    private String storageAdvice;
    private String rawExtractionNotes;

    public AiExtractionResponse() {}

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Instant getPreparationDateTime() { return preparationDateTime; }
    public void setPreparationDateTime(Instant preparationDateTime) { this.preparationDateTime = preparationDateTime; }

    public Instant getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(Instant pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }

    public String getStorageAdvice() { return storageAdvice; }
    public void setStorageAdvice(String storageAdvice) { this.storageAdvice = storageAdvice; }

    public String getRawExtractionNotes() { return rawExtractionNotes; }
    public void setRawExtractionNotes(String rawExtractionNotes) { this.rawExtractionNotes = rawExtractionNotes; }
}
