package com.annadhata.dto;

import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String name;
    private String email;
    private Role role;
    private String organizationName;
    private String organizationType;
    private VerificationStatus verificationStatus;
    private Double latitude;
    private Double longitude;
    private String address;

    public AuthResponse() {}

    public AuthResponse(String token, String id, String name, String email, Role role, 
                        String organizationName, String organizationType, VerificationStatus verificationStatus,
                        Double latitude, Double longitude, String address) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.organizationName = organizationName;
        this.organizationType = organizationType;
        this.verificationStatus = verificationStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
