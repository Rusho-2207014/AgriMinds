package com.agriminds.model;
import java.time.LocalDate;
public class WeatherAlert {
    private Long id;
    private String district;
    private String division;
    private LocalDate alertDate;
    private String alertType; 
    private String severity; 
    private String description;
    private String descriptionBengali;
    private String recommendations;
    private String recommendationsBengali;
    private Double temperature; 
    private Double rainfall; 
    private Double humidity; 
    private String windSpeed; 
    private boolean isActive;
    public WeatherAlert() {
        this.alertDate = LocalDate.now();
        this.isActive = true;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDescriptionBengali() { return descriptionBengali; }
    public void setDescriptionBengali(String descriptionBengali) { this.descriptionBengali = descriptionBengali; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public String getRecommendationsBengali() { return recommendationsBengali; }
    public void setRecommendationsBengali(String recommendationsBengali) { 
        this.recommendationsBengali = recommendationsBengali; 
    }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getRainfall() { return rainfall; }
    public void setRainfall(Double rainfall) { this.rainfall = rainfall; }
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    public String getWindSpeed() { return windSpeed; }
    public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
