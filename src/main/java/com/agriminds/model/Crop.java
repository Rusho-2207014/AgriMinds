package com.agriminds.model;
import java.time.LocalDateTime;
public class Crop {
    private Long id;
    private String cropName;
    private String cropNameBengali;
    private String scientificName;
    private String category; 
    private String season; 
    private Integer growingDays;
    private String soilType; 
    private Double waterRequirement; 
    private String climateRequirement;
    private String description;
    private String descriptionBengali;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    public Crop() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    public Crop(String cropName, String category, String season) {
        this();
        this.cropName = cropName;
        this.category = category;
        this.season = season;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCropName() {
        return cropName;
    }
    public void setCropName(String cropName) {
        this.cropName = cropName;
    }
    public String getCropNameBengali() {
        return cropNameBengali;
    }
    public void setCropNameBengali(String cropNameBengali) {
        this.cropNameBengali = cropNameBengali;
    }
    public String getScientificName() {
        return scientificName;
    }
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSeason() {
        return season;
    }
    public void setSeason(String season) {
        this.season = season;
    }
    public Integer getGrowingDays() {
        return growingDays;
    }
    public void setGrowingDays(Integer growingDays) {
        this.growingDays = growingDays;
    }
    public String getSoilType() {
        return soilType;
    }
    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }
    public Double getWaterRequirement() {
        return waterRequirement;
    }
    public void setWaterRequirement(Double waterRequirement) {
        this.waterRequirement = waterRequirement;
    }
    public String getClimateRequirement() {
        return climateRequirement;
    }
    public void setClimateRequirement(String climateRequirement) {
        this.climateRequirement = climateRequirement;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescriptionBengali() {
        return descriptionBengali;
    }
    public void setDescriptionBengali(String descriptionBengali) {
        this.descriptionBengali = descriptionBengali;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    @Override
    public String toString() {
        return "Crop{" +
                "id=" + id +
                ", cropName='" + cropName + '\'' +
                ", category='" + category + '\'' +
                ", season='" + season + '\'' +
                ", growingDays=" + growingDays +
                '}';
    }
}
