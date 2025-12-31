package com.agriminds.controller;

import com.agriminds.model.Expert;
import com.agriminds.model.Farmer;
import com.agriminds.model.Message;
import com.agriminds.model.Message.SenderType;
import com.agriminds.repository.ExpertRepository;
import com.agriminds.repository.FarmerRepository;
import com.agriminds.repository.MessageRepository;
import com.agriminds.repository.RatingRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for messaging between farmers and experts
 */
public class MessagesController {

    private static final Logger logger = LoggerFactory.getLogger(MessagesController.class);

    @FXML
    private VBox conversationsContainer;
    @FXML
    private VBox chatContainer;
    @FXML
    private Label chatHeaderLabel;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextArea messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Button startNewChatButton;

    private Farmer currentFarmer;
    private Expert currentExpert;
    private SenderType currentUserType;
    private MessageRepository messageRepository;
    private ExpertRepository expertRepository;
    private FarmerRepository farmerRepository;
    private RatingRepository ratingRepository;

    private String activeConversationId;
    private Long activeChatPartnerId;
    private String activeChatPartnerName;

    public MessagesController() {
        this.messageRepository = new MessageRepository();
        this.expertRepository = new ExpertRepository();
        this.farmerRepository = new FarmerRepository();
        this.ratingRepository = new RatingRepository();
    }

    public void setCurrentUser(Farmer farmer) {
        this.currentFarmer = farmer;
        this.currentUserType = SenderType.FARMER;
        Platform.runLater(this::loadConversations);
    }

    public void setCurrentUser(Expert expert) {
        this.currentExpert = expert;
        this.currentUserType = SenderType.EXPERT;
        Platform.runLater(this::loadConversations);
    }

    @FXML
    private void initialize() {
        chatContainer.setVisible(false);
        if (sendButton != null) {
            sendButton.setOnAction(e -> handleSendMessage());
        }
        if (startNewChatButton != null) {
            startNewChatButton.setOnAction(e -> handleStartNewChat());
        }
    }

    private void loadConversations() {
        if (conversationsContainer == null)
            return;

        conversationsContainer.getChildren().clear();

        Map<String, Message> conversations;
        if (currentUserType == SenderType.FARMER) {
            if (currentFarmer == null) {
                logger.warn("currentFarmer is null, cannot load conversations");
                return;
            }
            conversations = messageRepository.getFarmerConversations(currentFarmer.getId());
        } else {
            if (currentExpert == null) {
                logger.warn("currentExpert is null, cannot load conversations");
                return;
            }
            conversations = messageRepository.getExpertConversations(currentExpert.getId());
        }

        if (conversations.isEmpty()) {
            Label noConversations = new Label("No conversations yet");
            noConversations.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
            conversationsContainer.getChildren().add(noConversations);
        } else {
            for (Map.Entry<String, Message> entry : conversations.entrySet()) {
                VBox conversationCard = createConversationCard(entry.getValue());
                conversationsContainer.getChildren().add(conversationCard);
            }
        }
    }

    private VBox createConversationCard(Message lastMessage) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        final String partnerName;
        final Long partnerId;

        if (currentUserType == SenderType.FARMER) {
            // For farmer, show expert name with rating
            partnerId = lastMessage.getExpertId();
            Optional<Expert> expert = expertRepository.findById(partnerId);
            String expertName = expert.map(Expert::getFullName).orElse("Expert");

            // Add expert rating
            Double avgRating = ratingRepository.getAverageRating(partnerId);
            int ratingCount = ratingRepository.getRatingCount(partnerId);
            String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);
            partnerName = expertName + " " + ratingDisplay;
        } else {
            // For expert, show farmer name or other expert name
            if (lastMessage.getFarmerId() != null) {
                // Conversation with farmer
                partnerId = lastMessage.getFarmerId();
                Optional<Farmer> farmer = farmerRepository.findById(partnerId);
                partnerName = farmer.map(Farmer::getFullName).orElse("Farmer #" + partnerId);
            } else {
                // Expert-to-expert conversation - find the other expert
                Long otherExpertId = extractOtherExpertId(lastMessage.getConversationId(), currentExpert.getId());
                partnerId = otherExpertId;
                Optional<Expert> otherExpert = expertRepository.findById(otherExpertId);
                String expertName = otherExpert.map(Expert::getFullName).orElse("Expert #" + otherExpertId);

                // Add expert rating
                Double avgRating = ratingRepository.getAverageRating(otherExpertId);
                int ratingCount = ratingRepository.getRatingCount(otherExpertId);
                String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);
                partnerName = "🔬 " + expertName + " " + ratingDisplay;
            }
        }

        Label nameLabel = new Label(partnerName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #212121; -fx-font-weight: bold;");

        String previewText = lastMessage.getMessageText();
        if (previewText.length() > 50) {
            previewText = previewText.substring(0, 50) + "...";
        }

        Label messagePreview = new Label(previewText);
        messagePreview.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        messagePreview.setWrapText(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        Label dateLabel = new Label(lastMessage.getSentDate().format(formatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        // Add delete button
        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; " +
                "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 5;");
        deleteButton.setOnMouseEntered(
                e -> deleteButton.setStyle(deleteButton.getStyle() + "-fx-background-color: #ffebee;"));
        deleteButton.setOnMouseExited(
                e -> deleteButton.setStyle(deleteButton.getStyle().replace("-fx-background-color: #ffebee;", "")));
        deleteButton.setOnAction(e -> {
            e.consume();
            handleDeleteConversation(lastMessage.getConversationId(), partnerName);
        });

        // Header with name and delete button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        headerBox.getChildren().addAll(nameLabel, deleteButton);

        card.getChildren().addAll(headerBox, messagePreview, dateLabel);

        final VBox finalCard = card;
        card.setOnMouseClicked(e -> openChat(lastMessage.getConversationId(), partnerId, partnerName));
        card.setOnMouseEntered(e -> finalCard.setStyle(finalCard.getStyle() + " -fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(
                e -> finalCard.setStyle(finalCard.getStyle().replace(" -fx-background-color: #f5f5f5;", "")));

        return card;
    }

    @FXML
    private void handleStartNewChat() {
        if (currentUserType == SenderType.FARMER) {
            // Farmer starting chat with expert
            Dialog<Expert> dialog = new Dialog<>();
            dialog.setTitle("Start New Chat");
            dialog.setHeaderText("Select an expert to chat with:");

            ButtonType selectButtonType = new ButtonType("Start Chat", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            ListView<Expert> expertListView = new ListView<>();
            List<Expert> experts = expertRepository.findAll();
            expertListView.getItems().addAll(experts);

            expertListView.setCellFactory(param -> new ListCell<Expert>() {
                @Override
                protected void updateItem(Expert expert, boolean empty) {
                    super.updateItem(expert, empty);
                    if (empty || expert == null) {
                        setText(null);
                    } else {
                        Double avgRating = ratingRepository.getAverageRating(expert.getId());
                        int ratingCount = ratingRepository.getRatingCount(expert.getId());
                        String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);
                        setText(expert.getFullName() + " " + ratingDisplay + " - " + expert.getSpecialization());
                    }
                }
            });

            content.getChildren().add(expertListView);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == selectButtonType) {
                    return expertListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            Optional<Expert> result = dialog.showAndWait();
            result.ifPresent(expert -> {
                String conversationId = Message.generateConversationId(currentFarmer.getId(), expert.getId());
                openChat(conversationId, expert.getId(), expert.getFullName());
            });
        } else {
            // Expert starting chat with another expert
            if (currentExpert == null) {
                showError("Error: Expert not logged in. Please try again.");
                return;
            }

            Dialog<Expert> dialog = new Dialog<>();
            dialog.setTitle("Start New Chat with Expert");
            dialog.setHeaderText("Select an expert to collaborate with:");

            ButtonType selectButtonType = new ButtonType("Start Chat", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            ListView<Expert> expertListView = new ListView<>();
            List<Expert> experts = expertRepository.findAll();
            // Remove current expert from list
            experts.removeIf(e -> e.getId().equals(currentExpert.getId()));
            expertListView.getItems().addAll(experts);

            expertListView.setCellFactory(param -> new ListCell<Expert>() {
                @Override
                protected void updateItem(Expert expert, boolean empty) {
                    super.updateItem(expert, empty);
                    if (empty || expert == null) {
                        setText(null);
                    } else {
                        Double avgRating = ratingRepository.getAverageRating(expert.getId());
                        int ratingCount = ratingRepository.getRatingCount(expert.getId());
                        String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);
                        setText("🔬 " + expert.getFullName() + " " + ratingDisplay + " - "
                                + expert.getSpecialization());
                    }
                }
            });

            content.getChildren().add(expertListView);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == selectButtonType) {
                    return expertListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            Optional<Expert> result = dialog.showAndWait();
            result.ifPresent(otherExpert -> {
                String conversationId = Message.generateExpertConversationId(currentExpert.getId(),
                        otherExpert.getId());
                openChat(conversationId, otherExpert.getId(), otherExpert.getFullName());
            });
        }
    }

    private void openChat(String conversationId, Long partnerId, String partnerName) {
        this.activeConversationId = conversationId;
        this.activeChatPartnerId = partnerId;
        this.activeChatPartnerName = partnerName;

        // Check if this is an answer-specific conversation
        String headerText = "Chat with " + partnerName;
        if (conversationId.contains("_answer_")) {
            headerText = "💬 Reply Thread: " + partnerName;
        }
        chatHeaderLabel.setText(headerText);
        chatContainer.setVisible(true);

        // Mark messages as read
        messageRepository.markConversationAsRead(conversationId, currentUserType);

        // Load messages
        loadChatMessages();

        // Refresh conversation list to show this conversation
        loadConversations();
    }

    /**
     * Open a conversation for a specific answer (called from ExpertController)
     */
    public void openAnswerChat(Long farmerId, String farmerName, Long questionId, Long expertAnswerId) {
        // Generate the answer-specific conversation ID
        String conversationId = Message.generateAnswerConversationId(farmerId, expertAnswerId);

        // Open the chat
        openChat(conversationId, farmerId, farmerName);

        // Refresh conversations list to show this one as active
        loadConversations();
    }

    private void loadChatMessages() {
        chatScrollPane.setContent(null);

        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(15));
        messagesBox.setStyle("-fx-background-color: #f0f0f0;");

        List<Message> messages = messageRepository.getConversationMessages(activeConversationId);

        if (messages.isEmpty()) {
            Label noMessages = new Label("No messages yet. Start the conversation!");
            noMessages.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-padding: 20;");
            messagesBox.getChildren().add(noMessages);
        } else {
            for (Message message : messages) {
                HBox messageBox = createMessageBubble(message);
                messagesBox.getChildren().add(messageBox);
            }
        }

        chatScrollPane.setContent(messagesBox);
        chatScrollPane.setVvalue(1.0); // Scroll to bottom
    }

    private HBox createMessageBubble(Message message) {
        HBox container = new HBox();
        container.setPadding(new Insets(5));

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);

        boolean isMine = (currentUserType == SenderType.FARMER && message.getSenderType() == SenderType.FARMER) ||
                (currentUserType == SenderType.EXPERT && message.getSenderType() == SenderType.EXPERT);

        if (isMine) {
            // My message - align right, blue
            bubble.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 15; " +
                    "-fx-border-radius: 15;");
            container.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // Their message - align left, gray
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                    "-fx-border-color: #ddd; -fx-border-radius: 15;");
            container.setAlignment(Pos.CENTER_LEFT);
        }

        // Sender name with fallback
        String senderName = message.getSenderName();
        if (senderName == null || senderName.trim().isEmpty()) {
            senderName = isMine ? "You" : "Other";
        }
        Label senderLabel = new Label(senderName);
        senderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        if (isMine) {
            senderLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            senderLabel.setStyle("-fx-text-fill: #1976D2; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        Label messageLabel = new Label(message.getMessageText());
        messageLabel.setWrapText(true);
        if (isMine) {
            messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
        } else {
            messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");
        }

        // Show full date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");
        Label timeLabel = new Label(message.getSentDate().format(formatter));
        if (isMine) {
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #E3F2FD;");
        } else {
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        }

        bubble.getChildren().addAll(senderLabel, messageLabel, timeLabel);
        container.getChildren().add(bubble);

        return container;
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty() || activeConversationId == null) {
            return;
        }

        Long farmerId, expertId;
        String senderName;

        if (currentUserType == SenderType.FARMER) {
            farmerId = currentFarmer.getId();
            expertId = activeChatPartnerId;
            senderName = currentFarmer.getFullName();
        } else {
            // Expert sending message
            expertId = currentExpert.getId();
            senderName = currentExpert.getFullName();

            // Check if this is expert-to-expert conversation
            if (activeConversationId.startsWith("expert_")) {
                farmerId = null; // Expert-to-expert has no farmer
            } else {
                farmerId = activeChatPartnerId; // Expert replying to farmer
            }
        }

        Message message = new Message(
                activeConversationId,
                farmerId,
                expertId,
                currentUserType,
                senderName,
                messageText);

        // Copy question and answer IDs from existing conversation if available
        List<Message> existingMessages = messageRepository.getConversationMessages(activeConversationId);
        if (!existingMessages.isEmpty()) {
            Message firstMessage = existingMessages.get(0);
            message.setQuestionId(firstMessage.getQuestionId());
            message.setExpertAnswerId(firstMessage.getExpertAnswerId());
        }

        Message saved = messageRepository.save(message);
        if (saved != null) {
            messageInput.clear();
            loadChatMessages();
            loadConversations(); // Refresh conversation list
        } else {
            showError("Failed to send message. Please try again.");
        }
    }

    private void handleDeleteConversation(String conversationId, String partnerName) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Conversation");
        confirmAlert.setHeaderText("Delete conversation with " + partnerName + "?");
        confirmAlert.setContentText(
                "This will permanently delete all messages in this conversation. This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = messageRepository.deleteConversation(conversationId);
            if (deleted) {
                // Close chat if it's the active conversation
                if (conversationId.equals(activeConversationId)) {
                    chatContainer.setVisible(false);
                    activeConversationId = null;
                    activeChatPartnerId = null;
                }
                // Refresh conversation list
                loadConversations();

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Conversation deleted successfully.");
                successAlert.showAndWait();
            } else {
                showError("Failed to delete conversation. Please try again.");
            }
        }
    }

    /**
     * Extract the other expert ID from expert-to-expert conversation ID
     * Format: expert_123_expert_456
     */
    private Long extractOtherExpertId(String conversationId, Long currentExpertId) {
        if (conversationId.startsWith("expert_")) {
            String[] parts = conversationId.split("_");
            if (parts.length >= 4) {
                Long id1 = Long.parseLong(parts[1]);
                Long id2 = Long.parseLong(parts[3]);
                return id1.equals(currentExpertId) ? id2 : id1;
            }
        }
        return 0L;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
