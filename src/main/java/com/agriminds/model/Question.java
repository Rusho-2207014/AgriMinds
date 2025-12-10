package com.agriminds.model;
import java.time.LocalDateTime;
public class Question {
    private Long id;
    private Long farmerId;
    private String farmerName;
    private String category; 
    private String questionText;
    private String imageUrl; 
    private String status; 
    private LocalDateTime askedDate;
    private Long answeredByExpertId;
    private String expertName;
    private String answerText;
    private LocalDateTime answeredDate;
    public Question() {
    }
    public Question(Long farmerId, String category, String questionText) {
        this.farmerId = farmerId;
        this.category = category;
        this.questionText = questionText;
        this.status = "Open";
        this.askedDate = LocalDateTime.now();
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
    public String getFarmerName() {
        return farmerName;
    }
    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getQuestionText() {
        return questionText;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getAskedDate() {
        return askedDate;
    }
    public void setAskedDate(LocalDateTime askedDate) {
        this.askedDate = askedDate;
    }
    public Long getAnsweredByExpertId() {
        return answeredByExpertId;
    }
    public void setAnsweredByExpertId(Long answeredByExpertId) {
        this.answeredByExpertId = answeredByExpertId;
    }
    public String getExpertName() {
        return expertName;
    }
    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }
    public String getAnswerText() {
        return answerText;
    }
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
    public LocalDateTime getAnsweredDate() {
        return answeredDate;
    }
    public void setAnsweredDate(LocalDateTime answeredDate) {
        this.answeredDate = answeredDate;
    }
    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", farmerId=" + farmerId +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", askedDate=" + askedDate +
                '}';
    }
}
