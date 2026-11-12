package com.annadhata.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPassportRequest {

    @NotBlank(message = "Passport code or scanned QR payload is required")
    private String passportCode;

    private String verificationNotes;

    public VerifyPassportRequest() {}

    public String getPassportCode() { return passportCode; }
    public void setPassportCode(String passportCode) { this.passportCode = passportCode; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
}

