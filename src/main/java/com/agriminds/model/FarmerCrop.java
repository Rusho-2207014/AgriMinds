package com.agriminds.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class FarmerCrop {
    private Long id;
    private Long farmerId;
    private Long cropId;
    private String cropName;
    private Double quantity;
    private String unit;
    private Double sellingPrice;
    private LocalDate harvestDate;
    private boolean isAvailable;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public FarmerCrop() {
    }
    public FarmerCrop(Long farmerId, String cropName, Double quantity, String unit, Double sellingPrice) {
        this.farmerId = farmerId;
        this.cropName = cropName;
        this.quantity = quantity;
        this.unit = unit;
        this.sellingPrice = sellingPrice;
        this.isAvailable = true;
        this.createdAt = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFarmerId() {
        return farmerId;
    }
    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }
    public Long getCropId() {
        return cropId;
    }
    public void setCropId(Long cropId) {
        this.cropId = cropId;
    }
    public String getCropName() {
        return cropName;
    }
    public void setCropName(String cropName) {
        this.cropName = cropName;
    }
    public Double getQuantity() {
        return quantity;
    }
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public Double getSellingPrice() {
        return sellingPrice;
    }
    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    public LocalDate getHarvestDate() {
        return harvestDate;
    }
    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }
    public boolean isAvailable() {
        return isAvailable;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    @Override
    public String toString() {
        return "FarmerCrop{" +
                "id=" + id +
                ", cropName='" + cropName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
