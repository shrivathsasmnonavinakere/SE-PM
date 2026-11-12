package com.annadhata.dto;

import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;

import java.time.Instant;
import java.util.List;

public class UserProfileResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String address;
    private String organizationName;
    private String organizationType;
    private String registrationNumber;
    private VerificationStatus verificationStatus;
    private List<String> verificationDocuments;
    private Double latitude;
    private Double longitude;
    private Double trustScore;
    private String trustTier;
    private boolean active;
    private Instant createdAt;

    public UserProfileResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public List<String> getVerificationDocuments() { return verificationDocuments; }
    public void setVerificationDocuments(List<String> verificationDocuments) { this.verificationDocuments = verificationDocuments; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getTrustScore() { return trustScore; }
    public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
