package com.annadhata.model;

import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.enums.SafetyIndicator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "donations")
@CompoundIndexes({
    @CompoundIndex(name = "status_deadline_idx", def = "{'status': 1, 'pickupDeadline': 1}"),
    @CompoundIndex(name = "location_status_idx", def = "{'latitude': 1, 'longitude': 1, 'status': 1}")
})
public class Donation {

    @Id
    private String id;

    @Indexed
    private String donorId;

    private String donorName;
    private String donorPhone;

    private String foodName;
    private String foodDescription;

    @Indexed
    private FoodCategory foodCategory = FoodCategory.OTHER;

    private Double quantity;
    private Double remainingQuantity;
    private Double allocatedQuantity = 0.0;
    private List<String> activeClaimIds = new ArrayList<>();
    private Integer allocationCount = 0;
    private String measurementUnit = "kg";

    private String foodImage;

    private Instant preparationDateTime;

    @Indexed
    private Instant pickupDeadline;

    private String pickupAddress;
    private Double latitude;
    private Double longitude;

    private String foodHandlingDetails;
    private String storageInformation;

    @Indexed
    private DonationStatus status = DonationStatus.AVAILABLE;

    private SafetyIndicator safetyIndicator = SafetyIndicator.LOW_RISK;
    private String safetyNotes;

    private String claimedByReceiverId;
    private String claimedByReceiverName;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public Donation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }

    public FoodCategory getFoodCategory() { return foodCategory; }
    public void setFoodCategory(FoodCategory foodCategory) { this.foodCategory = foodCategory; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
        if (this.remainingQuantity == null && quantity != null) {
            this.remainingQuantity = quantity;
        }
    }

    public Double getRemainingQuantity() {
        return remainingQuantity != null ? remainingQuantity : quantity;
    }
    public void setRemainingQuantity(Double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public Double getAllocatedQuantity() { return allocatedQuantity != null ? allocatedQuantity : 0.0; }
    public void setAllocatedQuantity(Double allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public List<String> getActiveClaimIds() { return activeClaimIds; }
    public void setActiveClaimIds(List<String> activeClaimIds) { this.activeClaimIds = activeClaimIds; }

    public Integer getAllocationCount() { return allocationCount != null ? allocationCount : 0; }
    public void setAllocationCount(Integer allocationCount) { this.allocationCount = allocationCount; }

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

    public SafetyIndicator getSafetyIndicator() { return safetyIndicator; }
    public void setSafetyIndicator(SafetyIndicator safetyIndicator) { this.safetyIndicator = safetyIndicator; }

    public String getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(String safetyNotes) { this.safetyNotes = safetyNotes; }

    public String getClaimedByReceiverId() { return claimedByReceiverId; }
    public void setClaimedByReceiverId(String claimedByReceiverId) { this.claimedByReceiverId = claimedByReceiverId; }

    public String getClaimedByReceiverName() { return claimedByReceiverName; }
    public void setClaimedByReceiverName(String claimedByReceiverName) { this.claimedByReceiverName = claimedByReceiverName; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
