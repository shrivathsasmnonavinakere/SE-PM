package com.annadhata.dto;

import jakarta.validation.constraints.DecimalMin;

public class VolunteerProfileUpdateRequest {

    private String vehicleType;

    @DecimalMin(value = "1.0", message = "Max capacity must be at least 1 kg")
    private Double maxCapacityKg;

    @DecimalMin(value = "1.0", message = "Service radius must be at least 1 km")
    private Double serviceRadiusKm;

    private Boolean availableForDispatch;
    private Double latitude;
    private Double longitude;
    private String address;

    public VolunteerProfileUpdateRequest() {}

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Double getMaxCapacityKg() { return maxCapacityKg; }
    public void setMaxCapacityKg(Double maxCapacityKg) { this.maxCapacityKg = maxCapacityKg; }

    public Double getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(Double serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }

    public Boolean getAvailableForDispatch() { return availableForDispatch; }
    public void setAvailableForDispatch(Boolean availableForDispatch) { this.availableForDispatch = availableForDispatch; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

