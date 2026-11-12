package com.annadhata.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "matches")
public class MatchResult {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed
    private String receiverId;

    private String receiverName;
    private String receiverAddress;
    private Double receiverLatitude;
    private Double receiverLongitude;

    private Double distanceKm;
    private Double matchScore; // 0 to 100
    private Double distanceScore; // 0 to 100
    private Double quantityScore; // 0 to 100
    private Double urgencyScore; // 0 to 100
    private Double foodCompatibilityScore; // 0 to 100

    private String recommendationExplanation;
    private Instant generatedAt = Instant.now();

    public MatchResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public Double getReceiverLatitude() { return receiverLatitude; }
    public void setReceiverLatitude(Double receiverLatitude) { this.receiverLatitude = receiverLatitude; }

    public Double getReceiverLongitude() { return receiverLongitude; }
    public void setReceiverLongitude(Double receiverLongitude) { this.receiverLongitude = receiverLongitude; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public Double getDistanceScore() { return distanceScore; }
    public void setDistanceScore(Double distanceScore) { this.distanceScore = distanceScore; }

    public Double getQuantityScore() { return quantityScore; }
    public void setQuantityScore(Double quantityScore) { this.quantityScore = quantityScore; }

    public Double getUrgencyScore() { return urgencyScore; }
    public void setUrgencyScore(Double urgencyScore) { this.urgencyScore = urgencyScore; }

    public Double getFoodCompatibilityScore() { return foodCompatibilityScore; }
    public void setFoodCompatibilityScore(Double foodCompatibilityScore) { this.foodCompatibilityScore = foodCompatibilityScore; }

    public String getRecommendationExplanation() { return recommendationExplanation; }
    public void setRecommendationExplanation(String recommendationExplanation) { this.recommendationExplanation = recommendationExplanation; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
