package com.annadhata.model;

import com.annadhata.enums.FoodCategory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "impactRecords")
public class ImpactRecord {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed(sparse = true)
    private String claimId;

    @Indexed
    private String donorId;

    @Indexed
    private String receiverId;

    private FoodCategory foodCategory;
    private Double quantityKg;
    private Double mealsSaved; // e.g., quantityKg / 0.4 kg per meal (2.5 meals/kg)
    private Double co2AvoidedKg; // estimated 2.5 kg CO2e avoided per kg food redistributed
    private String cityOrArea;

    private LocalDate completionDate;

    @CreatedDate
    private Instant createdAt = Instant.now();

    public ImpactRecord() {}

    public ImpactRecord(String donationId, String donorId, String receiverId, FoodCategory foodCategory, 
                        Double quantityKg, String cityOrArea) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.receiverId = receiverId;
        this.foodCategory = foodCategory;
        this.quantityKg = quantityKg != null ? quantityKg : 0.0;
        this.mealsSaved = Math.round((this.quantityKg * 2.5) * 10.0) / 10.0; // 1 meal = 0.4 kg
        this.co2AvoidedKg = Math.round((this.quantityKg * 2.5) * 10.0) / 10.0; // 2.5 kg CO2e / kg food
        this.cityOrArea = cityOrArea;
        this.completionDate = LocalDate.now();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }

    public Double getMealsSaved() { return mealsSaved; }
    public void setMealsSaved(Double mealsSaved) { this.mealsSaved = mealsSaved; }

    public Double getCo2AvoidedKg() { return co2AvoidedKg; }
    public void setCo2AvoidedKg(Double co2AvoidedKg) { this.co2AvoidedKg = co2AvoidedKg; }

    public String getCityOrArea() { return cityOrArea; }
    public void setCityOrArea(String cityOrArea) { this.cityOrArea = cityOrArea; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
