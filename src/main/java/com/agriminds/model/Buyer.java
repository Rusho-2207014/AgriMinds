package com.agriminds.model;
import java.time.LocalDateTime;
public class Buyer {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String businessName;
    private String businessType; 
    private String tradeLicense;
    private String division;
    private String district;
    private String address;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private boolean isVerified;
    private boolean isActive;
    public Buyer() {
        this.registrationDate = LocalDateTime.now();
        this.isActive = true;
        this.isVerified = false;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getTradeLicense() { return tradeLicense; }
    public void setTradeLicense(String tradeLicense) { this.tradeLicense = tradeLicense; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
