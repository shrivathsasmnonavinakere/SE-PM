package com.annadhata.dto;

public class MatchRecommendationResponse {
    private String donationId;
    private String receiverId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Double receiverLatitude;
    private Double receiverLongitude;
    private Double distanceKm;
    private Double matchScore;
    private Double distanceScore;
    private Double quantityScore;
    private Double urgencyScore;
    private Double foodCompatibilityScore;
    private Double receiverTrustScore;
    private String receiverTrustTier;
    private Double trustBonusPoints;
    private String recommendationExplanation;

    public MatchRecommendationResponse() {}

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

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

    public Double getReceiverTrustScore() { return receiverTrustScore; }
    public void setReceiverTrustScore(Double receiverTrustScore) { this.receiverTrustScore = receiverTrustScore; }

    public String getReceiverTrustTier() { return receiverTrustTier; }
    public void setReceiverTrustTier(String receiverTrustTier) { this.receiverTrustTier = receiverTrustTier; }

    public Double getTrustBonusPoints() { return trustBonusPoints; }
    public void setTrustBonusPoints(Double trustBonusPoints) { this.trustBonusPoints = trustBonusPoints; }

    public String getRecommendationExplanation() { return recommendationExplanation; }
    public void setRecommendationExplanation(String recommendationExplanation) { this.recommendationExplanation = recommendationExplanation; }
}
