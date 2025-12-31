package com.agriminds.model;

import java.time.LocalDateTime;

/**
 * Represents a message in a conversation between a farmer and an expert.
 * Supports unlimited back-and-forth messaging.
 */
public class Message {

    public enum SenderType {
        FARMER, EXPERT
    }

    private Long id;
    private String conversationId; // Format: "farmer_{farmerId}_answer_{expertAnswerId}"
    private Long farmerId;
    private Long expertId;
    private Long questionId; // Which question this conversation is about
    private Long expertAnswerId; // Which specific expert answer this is replying to
    private SenderType senderType;
    private String senderName;
    private String messageText;
    private Boolean isRead;
    private LocalDateTime sentDate;

    // Constructors
    public Message() {
    }

    public Message(String conversationId, Long farmerId, Long expertId,
            SenderType senderType, String senderName, String messageText) {
        this.conversationId = conversationId;
        this.farmerId = farmerId;
        this.expertId = expertId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.messageText = messageText;
        this.isRead = false;
        this.sentDate = LocalDateTime.now();
    }

    // Static helper method to generate conversation ID
    public static String generateConversationId(Long farmerId, Long expertId) {
        return "farmer_" + farmerId + "_expert_" + expertId;
    }

    // Generate conversation ID specific to an expert answer
    public static String generateAnswerConversationId(Long farmerId, Long expertAnswerId) {
        return "farmer_" + farmerId + "_answer_" + expertAnswerId;
    }

    // Generate conversation ID for expert-to-expert communication
    public static String generateExpertConversationId(Long expertId1, Long expertId2) {
        // Always use smaller ID first for consistency
        long minId = Math.min(expertId1, expertId2);
        long maxId = Math.max(expertId1, expertId2);
        return "expert_" + minId + "_expert_" + maxId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getExpertAnswerId() {
        return expertAnswerId;
    }

    public void setExpertAnswerId(Long expertAnswerId) {
        this.expertAnswerId = expertAnswerId;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId='" + conversationId + '\'' +
                ", senderType=" + senderType +
                ", senderName='" + senderName + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}
