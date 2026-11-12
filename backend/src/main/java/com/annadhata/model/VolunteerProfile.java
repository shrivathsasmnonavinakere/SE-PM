package com.annadhata.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "volunteerProfiles")
public class VolunteerProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String userName;
    private String phone;
    private String vehicleType = "Two-Wheeler / Scooter";
    private Double maxCapacityKg = 25.0;
    private Double serviceRadiusKm = 15.0;
    private boolean availableForDispatch = true;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer completedDeliveriesCount = 0;
    private Double totalKgTransported = 0.0;

    @CreatedDate
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public VolunteerProfile() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Double getMaxCapacityKg() { return maxCapacityKg; }
    public void setMaxCapacityKg(Double maxCapacityKg) { this.maxCapacityKg = maxCapacityKg; }

    public Double getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(Double serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }

    public boolean isAvailableForDispatch() { return availableForDispatch; }
    public void setAvailableForDispatch(boolean availableForDispatch) { this.availableForDispatch = availableForDispatch; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getCompletedDeliveriesCount() { return completedDeliveriesCount != null ? completedDeliveriesCount : 0; }
    public void setCompletedDeliveriesCount(Integer completedDeliveriesCount) { this.completedDeliveriesCount = completedDeliveriesCount; }

    public Double getTotalKgTransported() { return totalKgTransported != null ? totalKgTransported : 0.0; }
    public void setTotalKgTransported(Double totalKgTransported) { this.totalKgTransported = totalKgTransported; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

