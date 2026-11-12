package com.annadhata.model;

import com.annadhata.enums.PassportStatus;
import com.annadhata.enums.SafetyIndicator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "foodPassports")
public class FoodPassport {

    @Id
    private String id;

    @Indexed(unique = true)
    private String claimId;

    @Indexed
    private String donationId;

    @Indexed
    private String donorId;

    @Indexed
    private String receiverId;

    private String donorName;
    private String receiverName;
    private String foodName;
    private Double allocatedQuantity;
    private String measurementUnit;

    @Indexed(unique = true)
    private String passportCode;

    private String hmacSignature;
    private String qrPayloadJson;
    private String qrImageBase64;

    @Indexed
    private PassportStatus status = PassportStatus.ACTIVE;

    private SafetyIndicator safetyIndicator;

    private Instant issuedAt = Instant.now();
    private Instant expiresAt;
    private Instant verifiedAt;

    private String verifiedByUserId;
    private String verifiedByName;
    private String verificationNotes;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public FoodPassport() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Double getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Double allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public String getPassportCode() { return passportCode; }
    public void setPassportCode(String passportCode) { this.passportCode = passportCode; }

    public String getHmacSignature() { return hmacSignature; }
    public void setHmacSignature(String hmacSignature) { this.hmacSignature = hmacSignature; }

    public String getQrPayloadJson() { return qrPayloadJson; }
    public void setQrPayloadJson(String qrPayloadJson) { this.qrPayloadJson = qrPayloadJson; }

    public String getQrImageBase64() { return qrImageBase64; }
    public void setQrImageBase64(String qrImageBase64) { this.qrImageBase64 = qrImageBase64; }

    public PassportStatus getStatus() { return status; }
    public void setStatus(PassportStatus status) { this.status = status; }

    public SafetyIndicator getSafetyIndicator() { return safetyIndicator; }
    public void setSafetyIndicator(SafetyIndicator safetyIndicator) { this.safetyIndicator = safetyIndicator; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(String verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }

    public String getVerifiedByName() { return verifiedByName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

