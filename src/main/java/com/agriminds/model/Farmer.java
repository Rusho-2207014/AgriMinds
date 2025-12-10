package com.agriminds.model;
import java.time.LocalDateTime;
public class Farmer {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String nationalId;
    private String division; 
    private String district;
    private String upazila;
    private String village;
    private Double farmSize; 
    private String farmingType; 
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private String language; 
    public Farmer() {
        this.registrationDate = LocalDateTime.now();
        this.isActive = true;
        this.language = "Bengali";
    }
    public Farmer(String fullName, String email, String phoneNumber) {
        this();
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getNationalId() {
        return nationalId;
    }
    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
    public String getDivision() {
        return division;
    }
    public void setDivision(String division) {
        this.division = division;
    }
    public String getDistrict() {
        return district;
    }
    public void setDistrict(String district) {
        this.district = district;
    }
    public String getUpazila() {
        return upazila;
    }
    public void setUpazila(String upazila) {
        this.upazila = upazila;
    }
    public String getVillage() {
        return village;
    }
    public void setVillage(String village) {
        this.village = village;
    }
    public Double getFarmSize() {
        return farmSize;
    }
    public void setFarmSize(Double farmSize) {
        this.farmSize = farmSize;
    }
    public String getFarmingType() {
        return farmingType;
    }
    public void setFarmingType(String farmingType) {
        this.farmingType = farmingType;
    }
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    @Override
    public String toString() {
        return "Farmer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", district='" + district + '\'' +
                ", farmSize=" + farmSize +
                '}';
    }
}
