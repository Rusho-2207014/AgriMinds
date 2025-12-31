package com.agriminds.model;

import java.time.LocalDateTime;

/**
 * Represents an expert's answer to a farmer's question.
 * Multiple experts can answer the same question.
 */
public class ExpertAnswer {
    private Long id;
    private Long questionId;
    private Long expertId;
    private String expertName;
    private Long parentAnswerId; // For replies/corrections to other experts' answers
    private String replyType; // 'correction', 'addition', 'reply', or null for original
    private Boolean accepted; // NULL for regular answers, TRUE for accepted corrections, FALSE for denied
    private String answerText;
    private LocalDateTime answeredDate;
    private LocalDateTime createdAt;

    // Constructors
    public ExpertAnswer() {
    }

    public ExpertAnswer(Long questionId, Long expertId, String expertName, String answerText) {
        this.questionId = questionId;
        this.expertId = expertId;
        this.expertName = expertName;
        this.answerText = answerText;
        this.answeredDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getParentAnswerId() {
        return parentAnswerId;
    }

    public void setParentAnswerId(Long parentAnswerId) {
        this.parentAnswerId = parentAnswerId;
    }

    public String getReplyType() {
        return replyType;
    }

    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    @Override
    public String toString() {
        return "ExpertAnswer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", expertId=" + expertId +
                ", expertName='" + expertName + '\'' +
                ", parentAnswerId=" + parentAnswerId +
                ", replyType='" + replyType + '\'' +
                ", answeredDate=" + answeredDate +
                '}';
    }
}
