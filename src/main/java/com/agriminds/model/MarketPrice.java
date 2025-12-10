package com.agriminds.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class MarketPrice {
    private Long id;
    private Long cropId;
    private String cropName;
    private String marketName;
    private String district;
    private String division;
    private Double wholesalePrice; 
    private Double retailPrice; 
    private String unit; 
    private LocalDate priceDate;
    private String source; 
    private LocalDateTime recordedAt;
    private Double priceChange; 
    public MarketPrice() {
        this.recordedAt = LocalDateTime.now();
        this.priceDate = LocalDate.now();
        this.unit = "kg";
    }
    public MarketPrice(String cropName, String marketName, Double retailPrice) {
        this();
        this.cropName = cropName;
        this.marketName = marketName;
        this.retailPrice = retailPrice;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public String getMarketName() {
        return marketName;
    }
    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }
    public String getDistrict() {
        return district;
    }
    public void setDistrict(String district) {
        this.district = district;
    }
    public String getDivision() {
        return division;
    }
    public void setDivision(String division) {
        this.division = division;
    }
    public Double getWholesalePrice() {
        return wholesalePrice;
    }
    public void setWholesalePrice(Double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }
    public Double getRetailPrice() {
        return retailPrice;
    }
    public void setRetailPrice(Double retailPrice) {
        this.retailPrice = retailPrice;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public LocalDate getPriceDate() {
        return priceDate;
    }
    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
    public Double getPriceChange() {
        return priceChange;
    }
    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }
    @Override
    public String toString() {
        return "MarketPrice{" +
                "cropName='" + cropName + '\'' +
                ", marketName='" + marketName + '\'' +
                ", retailPrice=" + retailPrice +
                ", unit='" + unit + '\'' +
                ", priceDate=" + priceDate +
                '}';
    }
}
