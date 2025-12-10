package com.agriminds.model;
import java.time.LocalDate;
public class SoilHealth {
    private Long id;
    private Long farmerId;
    private LocalDate testDate;
    private Double pH;
    private Double nitrogen; 
    private Double phosphorus; 
    private Double potassium; 
    private Double organicMatter; 
    private String soilType;
    private String soilTexture;
    private Double moisture; 
    private String healthStatus; 
    private String recommendations;
    private String recommendationsBengali;
    public SoilHealth() {
        this.testDate = LocalDate.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public Double getpH() { return pH; }
    public void setpH(Double pH) { this.pH = pH; }
    public Double getNitrogen() { return nitrogen; }
    public void setNitrogen(Double nitrogen) { this.nitrogen = nitrogen; }
    public Double getPhosphorus() { return phosphorus; }
    public void setPhosphorus(Double phosphorus) { this.phosphorus = phosphorus; }
    public Double getPotassium() { return potassium; }
    public void setPotassium(Double potassium) { this.potassium = potassium; }
    public Double getOrganicMatter() { return organicMatter; }
    public void setOrganicMatter(Double organicMatter) { this.organicMatter = organicMatter; }
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    public String getSoilTexture() { return soilTexture; }
    public void setSoilTexture(String soilTexture) { this.soilTexture = soilTexture; }
    public Double getMoisture() { return moisture; }
    public void setMoisture(Double moisture) { this.moisture = moisture; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public String getRecommendationsBengali() { return recommendationsBengali; }
    public void setRecommendationsBengali(String recommendationsBengali) { 
        this.recommendationsBengali = recommendationsBengali; 
    }
}
