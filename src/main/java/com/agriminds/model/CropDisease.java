package com.agriminds.model;
import java.time.LocalDateTime;
public class CropDisease {
    private Long id;
    private Long cropId;
    private String diseaseName;
    private String diseaseNameBengali;
    private String symptoms;
    private String symptomsBengali;
    private String causes;
    private String severity; 
    private String treatment;
    private String treatmentBengali;
    private String preventiveMeasures;
    private String preventiveMeasuresBengali;
    private String imageUrl;
    private LocalDateTime createdDate;
    public CropDisease() {
        this.createdDate = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCropId() { return cropId; }
    public void setCropId(Long cropId) { this.cropId = cropId; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public String getDiseaseNameBengali() { return diseaseNameBengali; }
    public void setDiseaseNameBengali(String diseaseNameBengali) { this.diseaseNameBengali = diseaseNameBengali; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getSymptomsBengali() { return symptomsBengali; }
    public void setSymptomsBengali(String symptomsBengali) { this.symptomsBengali = symptomsBengali; }
    public String getCauses() { return causes; }
    public void setCauses(String causes) { this.causes = causes; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public String getTreatmentBengali() { return treatmentBengali; }
    public void setTreatmentBengali(String treatmentBengali) { this.treatmentBengali = treatmentBengali; }
    public String getPreventiveMeasures() { return preventiveMeasures; }
    public void setPreventiveMeasures(String preventiveMeasures) { this.preventiveMeasures = preventiveMeasures; }
    public String getPreventiveMeasuresBengali() { return preventiveMeasuresBengali; }
    public void setPreventiveMeasuresBengali(String preventiveMeasuresBengali) { 
        this.preventiveMeasuresBengali = preventiveMeasuresBengali; 
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
