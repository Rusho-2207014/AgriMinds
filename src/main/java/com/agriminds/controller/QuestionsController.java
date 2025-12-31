package com.agriminds.controller;

import com.agriminds.model.ExpertAnswer;
import com.agriminds.model.Farmer;
import com.agriminds.model.Message;
import com.agriminds.model.Question;
import com.agriminds.model.Rating;
import com.agriminds.repository.ExpertAnswerRepository;
import com.agriminds.repository.MessageRepository;
import com.agriminds.repository.QuestionRepository;
import com.agriminds.repository.RatingRepository;
import com.agriminds.service.AIService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class QuestionsController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionsController.class);

    @FXML
    private Button askQuestionBtn;
    @FXML
    private Button deleteAllBtn;
    @FXML
    private VBox questionsContainer;

    private Farmer currentUser;
    private QuestionRepository questionRepository;
    private ExpertAnswerRepository expertAnswerRepository;
    private AIService aiService;
    private MessageRepository messageRepository;
    private RatingRepository ratingRepository;
    private TabPane mainTabPane;
    private Tab messagesTab;

    public QuestionsController() {
        this.questionRepository = new QuestionRepository();
        this.expertAnswerRepository = new ExpertAnswerRepository();
        this.aiService = new AIService();
        this.messageRepository = new MessageRepository();
        this.ratingRepository = new RatingRepository();
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        refreshQuestionsList();
    }

    public void setMainTabPane(TabPane tabPane) {
        this.mainTabPane = tabPane;
    }

    public void setMessagesTab(Tab tab) {
        this.messagesTab = tab;
    }

    @FXML
    private void handleAskQuestion() {
        showAskQuestionDialog();
    }

    @FXML
    private void handleDeleteAllQuestions() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please login to delete questions.");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete All");
        confirmAlert.setHeaderText("Delete All Questions?");
        confirmAlert
                .setContentText("Are you sure you want to delete all your questions? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int deletedCount = questionRepository.deleteAllByFarmerId(currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Successfully deleted " + deletedCount + " question(s).");
                    refreshQuestionsList();
                } catch (Exception e) {
                    logger.error("Error deleting all questions", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete questions. Please try again.");
                }
            }
        });
    }

    private void refreshQuestionsList() {
        questionsContainer.getChildren().clear();

        if (currentUser == null) {
            Label placeholder = new Label("Please login to view your questions.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            questionsContainer.getChildren().add(placeholder);
            return;
        }

        List<Question> questions = questionRepository.findByFarmerId(currentUser.getId());

        if (questions.isEmpty()) {
            Label placeholder = new Label(
                    "Your questions and expert answers will appear here.\nClick 'Ask New Question' to get help from agricultural experts.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            questionsContainer.getChildren().add(placeholder);
        } else {
            for (Question question : questions) {
                VBox questionCard = createQuestionCard(question);
                questionsContainer.getChildren().add(questionCard);
            }
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = new Label("📁 " + question.getCategory());
        categoryLabel.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 5 10; " +
                "-fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 12px;");

        Label statusLabel = new Label(question.getStatus());
        if ("Answered".equals(question.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; " +
                    "-fx-padding: 5 10; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 12px;");
        } else {
            statusLabel.setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; " +
                    "-fx-padding: 5 10; -fx-border-radius: 3; -fx-background-radius: 3; -fx-font-size: 12px;");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        Label dateLabel = new Label("🕒 " + question.getAskedDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(categoryLabel, statusLabel, spacer, dateLabel);

        Label questionLabel = new Label("Q: " + question.getQuestionText());
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-text-fill: #333;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewAnswerBtn = new Button("👁 View Answer");
        viewAnswerBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 15; -fx-cursor: hand;");
        viewAnswerBtn.setOnAction(e -> showAnswerDialog(question));

        Button aiAnswerBtn = new Button("🤖 Get AI Answer");
        aiAnswerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 15; -fx-cursor: hand;");
        aiAnswerBtn.setOnAction(e -> generateAIAnswer(question));

        // If already has AI answer, change button text
        if (question.getAiGenerated() != null && question.getAiGenerated()) {
            aiAnswerBtn.setText("✅ AI Answered");
            aiAnswerBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 6 15; -fx-cursor: hand;");
        }

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 15; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete this question?");
            confirm.setContentText("Are you sure you want to delete this question?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    questionRepository.delete(question.getId());
                    refreshQuestionsList();
                }
            });
        });

        buttonBox.getChildren().addAll(viewAnswerBtn, aiAnswerBtn, deleteBtn);

        card.getChildren().addAll(headerBox, questionLabel, buttonBox);
        return card;
    }

    private void showAnswerDialog(Question question) {
        Stage dialog = new Stage();
        dialog.setTitle("Question & Answer");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("Question Details");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1976D2;");

        VBox questionBox = new VBox(8);
        questionBox.setPadding(new Insets(15));
        questionBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label categoryLabel = new Label("Category: " + question.getCategory());
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label questionLabel = new Label("Q: " + question.getQuestionText());
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        questionLabel.setWrapText(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        Label askedDateLabel = new Label("Asked on: " + question.getAskedDate().format(formatter));
        askedDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        questionBox.getChildren().addAll(categoryLabel, questionLabel, askedDateLabel);

        // Check if we have any answers
        boolean hasAIAnswer = question.getAiAnswerText() != null && !question.getAiAnswerText().trim().isEmpty();

        // Get all expert answers from new table
        List<ExpertAnswer> expertAnswers = expertAnswerRepository.getAnswersByQuestionId(question.getId());

        VBox answersContainer = new VBox(15);
        answersContainer.setPadding(new Insets(0));

        // AI Answer Section
        if (hasAIAnswer) {
            VBox aiAnswerBox = new VBox(8);
            aiAnswerBox.setPadding(new Insets(15));
            aiAnswerBox.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");

            Label aiTitleLabel = new Label("🤖 AI Generated Answer:");
            aiTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            aiTitleLabel.setStyle("-fx-text-fill: #1976D2;");

            Label aiAnswerLabel = new Label("A: " + question.getAiAnswerText());
            aiAnswerLabel.setWrapText(true);
            aiAnswerLabel.setStyle("-fx-font-size: 14px;");

            Label aiExpertLabel = new Label("🤖 Answered by: AI Agricultural Advisor (Instant Response)");
            aiExpertLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #1976D2; -fx-font-style: italic; -fx-font-weight: bold;");

            if (question.getAiAnsweredDate() != null) {
                Label aiDateLabel = new Label("on " + question.getAiAnsweredDate().format(formatter));
                aiDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
                aiAnswerBox.getChildren().addAll(aiTitleLabel, aiAnswerLabel, aiExpertLabel, aiDateLabel);
            } else {
                aiAnswerBox.getChildren().addAll(aiTitleLabel, aiAnswerLabel, aiExpertLabel);
            }

            answersContainer.getChildren().add(aiAnswerBox);
        }

        // Expert Answers Section - Display all expert answers
        if (!expertAnswers.isEmpty()) {
            for (int i = 0; i < expertAnswers.size(); i++) {
                ExpertAnswer expertAnswer = expertAnswers.get(i);

                VBox expertAnswerBox = new VBox(8);
                expertAnswerBox.setPadding(new Insets(15));
                expertAnswerBox.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5;");

                String titleText = expertAnswers.size() > 1
                        ? "👨‍🌾 Expert Answer #" + (i + 1) + ":"
                        : "👨‍🌾 Expert Answer:";
                Label expertTitleLabel = new Label(titleText);
                expertTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                expertTitleLabel.setStyle("-fx-text-fill: #2E7D32;");

                Label expertAnswerLabel = new Label("A: " + expertAnswer.getAnswerText());
                expertAnswerLabel.setWrapText(true);
                expertAnswerLabel.setStyle("-fx-font-size: 14px;");

                // Get rating info for this expert
                Double avgRating = ratingRepository.getAverageRating(expertAnswer.getExpertId());
                int ratingCount = ratingRepository.getRatingCount(expertAnswer.getExpertId());
                String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);

                Label expertLabel = new Label(
                        "👨‍🌾 Answered by: " + expertAnswer.getExpertName() + " " + ratingDisplay);
                expertLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

                HBox expertInfoBox = new HBox(10);
                expertInfoBox.setAlignment(Pos.CENTER_LEFT);
                expertInfoBox.getChildren().add(expertLabel);

                if (expertAnswer.getAnsweredDate() != null) {
                    Label expertDateLabel = new Label("on " + expertAnswer.getAnsweredDate().format(formatter));
                    expertDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
                    expertInfoBox.getChildren().add(expertDateLabel);
                }

                // Buttons container
                HBox buttonsBox = new HBox(10);
                buttonsBox.setAlignment(Pos.CENTER_LEFT);

                // Add Reply button to message this expert
                Button replyButton = new Button("💬 Reply");
                replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 15;");
                final Long expertId = expertAnswer.getExpertId();
                final String expertName = expertAnswer.getExpertName();
                final Long expertAnswerId = expertAnswer.getId();
                final Long questionId = question.getId();
                replyButton.setOnAction(e -> openMessageDialog(expertId, expertName, questionId, expertAnswerId));

                // Check if farmer already rated this answer
                Rating existingRating = ratingRepository.getRatingByFarmerAndAnswer(currentUser.getId(),
                        expertAnswerId);

                if (existingRating != null) {
                    // Show existing rating
                    Label yourRatingLabel = new Label("Your Rating: " + "⭐".repeat(existingRating.getRating()));
                    yourRatingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");

                    Button editRateButton = new Button("✏️ Edit Rating");
                    editRateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                            "-fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 15;");
                    editRateButton.setOnAction(e -> openRatingDialog(expertId, expertName, expertAnswerId));

                    buttonsBox.getChildren().addAll(replyButton, yourRatingLabel, editRateButton);
                } else {
                    // Show rate button
                    Button rateButton = new Button("⭐ Rate Answer");
                    rateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                            "-fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 15;");
                    rateButton.setOnAction(e -> openRatingDialog(expertId, expertName, expertAnswerId));

                    buttonsBox.getChildren().addAll(replyButton, rateButton);
                }

                // Build the expertAnswerBox with all elements first
                expertAnswerBox.getChildren().addAll(expertTitleLabel, expertAnswerLabel, expertInfoBox, buttonsBox);

                // Show replies/corrections to this expert answer
                List<ExpertAnswer> replies = expertAnswerRepository.getRepliesByParentAnswerId(expertAnswer.getId());
                if (!replies.isEmpty()) {
                    for (ExpertAnswer reply : replies) {
                        VBox replyBox = new VBox(6);
                        replyBox.setPadding(new Insets(10, 10, 10, 30)); // Indent replies

                        // Color based on acceptance status
                        String bgColor, borderColor;
                        if (reply.getAccepted() == null) {
                            bgColor = "#FFF9C4"; // Yellow - pending
                            borderColor = "#FBC02D";
                        } else if (reply.getAccepted()) {
                            bgColor = "#C8E6C9"; // Green - accepted
                            borderColor = "#66BB6A";
                        } else {
                            bgColor = "#FFCDD2"; // Red - denied
                            borderColor = "#EF5350";
                        }

                        replyBox.setStyle(
                                "-fx-background-color: " + bgColor + "; -fx-background-radius: 6; -fx-border-color: "
                                        + borderColor + "; -fx-border-width: 1; -fx-border-radius: 6;");

                        String replyTypeLabel = reply.getReplyType() != null ? reply.getReplyType().toUpperCase()
                                : "REPLY";
                        String statusLabel = reply.getAccepted() == null ? " (Pending Approval)"
                                : reply.getAccepted() ? " ✓ Accepted" : " ✗ Denied";
                        Label replyTitle = new Label(
                                "↳ " + replyTypeLabel + " by Expert " + reply.getExpertName() + statusLabel);
                        replyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                        replyTitle.setStyle("-fx-text-fill: #F57F17;");

                        Label replyText = new Label(reply.getAnswerText());
                        replyText.setWrapText(true);
                        replyText.setStyle("-fx-font-size: 13px;");

                        Label replyDate = new Label(reply.getAnsweredDate() != null
                                ? "Posted on " + reply.getAnsweredDate().format(formatter)
                                : "");
                        replyDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

                        replyBox.getChildren().addAll(replyTitle, replyText, replyDate);

                        // Add Accept/Deny buttons if not yet decided
                        if (reply.getAccepted() == null) {
                            HBox actionButtons = new HBox(10);
                            actionButtons.setPadding(new Insets(5, 0, 0, 0));

                            Button acceptBtn = new Button("✓ Accept");
                            acceptBtn.setStyle(
                                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand;");
                            acceptBtn.setOnAction(e -> {
                                if (expertAnswerRepository.acceptCorrection(reply.getId())) {
                                    showAlert(Alert.AlertType.INFORMATION, "Correction Accepted",
                                            "You have accepted this " + reply.getReplyType()
                                                    + ". It will now count towards the expert's statistics.");
                                    showAnswerDialog(question); // Refresh
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept correction.");
                                }
                            });

                            Button denyBtn = new Button("✗ Deny");
                            denyBtn.setStyle(
                                    "-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand;");
                            denyBtn.setOnAction(e -> {
                                if (expertAnswerRepository.denyCorrection(reply.getId())) {
                                    showAlert(Alert.AlertType.INFORMATION, "Correction Denied",
                                            "You have denied this " + reply.getReplyType()
                                                    + ". It will not count towards the expert's statistics.");
                                    showAnswerDialog(question); // Refresh
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to deny correction.");
                                }
                            });

                            actionButtons.getChildren().addAll(acceptBtn, denyBtn);
                            replyBox.getChildren().add(actionButtons);
                        }

                        expertAnswerBox.getChildren().add(replyBox);
                    }
                }

                // Add recent message if exists
                Message latestMessage = messageRepository.getLatestMessageForAnswer(expertAnswerId);
                if (latestMessage != null) {
                    VBox recentMessageBox = new VBox(5);
                    recentMessageBox.setPadding(new Insets(8));
                    recentMessageBox.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #BDBDBD; " +
                            "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 1;");

                    Label recentLabel = new Label("💬 Recent Message:");
                    recentLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #666;");

                    String senderPrefix = latestMessage.getSenderType() == Message.SenderType.FARMER ? "You"
                            : expertName;
                    String messagePreview = latestMessage.getMessageText();
                    if (messagePreview.length() > 80) {
                        messagePreview = messagePreview.substring(0, 80) + "...";
                    }

                    Label messageLabel = new Label(senderPrefix + ": " + messagePreview);
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-style: italic;");

                    Label timeLabel = new Label(latestMessage.getSentDate().format(formatter));
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

                    recentMessageBox.getChildren().addAll(recentLabel, messageLabel, timeLabel);
                    expertAnswerBox.getChildren().add(recentMessageBox);
                }

                answersContainer.getChildren().add(expertAnswerBox);
            }
        }

        // No Answers Section
        if (!hasAIAnswer && expertAnswers.isEmpty()) {
            VBox noAnswerBox = new VBox(8);
            noAnswerBox.setPadding(new Insets(15));
            noAnswerBox.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");

            Label answerTitleLabel = new Label("Answer:");
            answerTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            answerTitleLabel.setStyle("-fx-text-fill: #2E7D32;");

            Label noAnswerLabel = new Label("⏳ No answer yet. An expert will respond soon.");
            noAnswerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #E65100;");
            noAnswerLabel.setWrapText(true);

            noAnswerBox.getChildren().addAll(answerTitleLabel, noAnswerLabel);
            answersContainer.getChildren().add(noAnswerBox);
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-padding: 8 30;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, questionBox, answersContainer, buttonBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5;");

        dialog.setScene(new javafx.scene.Scene(scrollPane, 600, 500));
        dialog.showAndWait();
    }

    private void showAskQuestionDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Ask Question to Expert");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("Ask Agricultural Expert");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Disease (রোগ)", "Pest (পোকামাকড়)", "Soil (মাটি)",
                "Weather (আবহাওয়া)", "Fertilizer (সার)", "General (সাধারণ)");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(300);

        Label questionLabel = new Label("Your Question:");
        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Describe your problem or question in detail...");
        questionArea.setPrefRowCount(6);
        questionArea.setWrapText(true);

        grid.add(categoryLabel, 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(questionLabel, 0, 1);
        grid.add(questionArea, 1, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button submitBtn = new Button("Submit Question");
        submitBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 8 20;");
        submitBtn.setOnAction(e -> {
            if (categoryCombo.getValue() == null || questionArea.getText().length() < 10) {
                showError("Validation Error", "Please select category and write your question");
                return;
            }

            try {
                Question question = new Question();
                question.setFarmerId(currentUser.getId());
                question.setFarmerName(currentUser.getFullName());
                question.setCategory(categoryCombo.getValue());
                question.setQuestionText(questionArea.getText());
                question.setStatus("Open");
                question.setAskedDate(java.time.LocalDateTime.now());
                question.setAiGenerated(false);

                Long questionId = questionRepository.save(question);

                if (questionId != null) {
                    showInfo("Success",
                            "Your question has been submitted!\n\nClick 'Get AI Answer' for instant AI response or wait for an expert.");
                    refreshQuestionsList();
                    dialog.close();
                } else {
                    showError("Error", "Failed to submit question. Please try again.");
                }
            } catch (Exception ex) {
                logger.error("Error submitting question", ex);
                showError("Error", "Failed to submit question: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #ccc; -fx-padding: 8 20;");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        root.getChildren().addAll(title, grid, buttonBox);

        dialog.setScene(new javafx.scene.Scene(root, 500, 400));
        dialog.showAndWait();
    }

    private void generateAIAnswer(Question question) {
        // Create custom loading dialog with timer
        Stage loadingDialog = new Stage();
        loadingDialog.initModality(Modality.APPLICATION_MODAL);
        loadingDialog.setTitle("AI Processing");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setStyle("-fx-background-color: white;");

        Label headerLabel = new Label("🤖 Generating AI Answer...");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLabel.setStyle("-fx-text-fill: #1976D2;");

        Label messageLabel = new Label("Please wait while AI analyzes your question...");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #666;");

        Label timerLabel = new Label("Time elapsed: 0 seconds");
        timerLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Indeterminate

        dialogContent.getChildren().addAll(headerLabel, messageLabel, progressIndicator, timerLabel);
        loadingDialog.setScene(new javafx.scene.Scene(dialogContent, 350, 200));
        loadingDialog.show();

        // Timer to update elapsed time
        final long[] startTime = { System.currentTimeMillis() };
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.1), e -> {
                    long elapsed = (System.currentTimeMillis() - startTime[0]) / 1000;
                    timerLabel.setText("Time elapsed: " + elapsed + " seconds");
                }));
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();

        // Run AI generation in background thread
        new Thread(() -> {
            try {
                logger.info("Requesting AI answer for question ID: {}", question.getId());
                String aiAnswer = aiService.getAIAnswer(
                        question.getCategory(),
                        question.getQuestionText());

                final long totalTime = (System.currentTimeMillis() - startTime[0]) / 1000;

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    timeline.stop();
                    loadingDialog.close();

                    if (aiAnswer != null &&
                            !aiAnswer.contains("unavailable") &&
                            !aiAnswer.contains("Unable to generate") &&
                            aiAnswer.length() > 50) {

                        // Update question with AI answer
                        question.setAiAnswerText(aiAnswer);
                        question.setAiAnsweredDate(java.time.LocalDateTime.now());
                        question.setStatus("Answered");
                        question.setAiGenerated(true);

                        // Save AI answer using new method (doesn't overwrite expert answer)
                        questionRepository.updateAIAnswer(question.getId(), aiAnswer);

                        logger.info("AI answer generated successfully in {} seconds", totalTime);
                        showInfo("AI Answer Generated",
                                "✅ AI has successfully answered your question!\n\n" +
                                        "⏱️ Generated in " + totalTime + " seconds\n\n" +
                                        "Click 'View Answer' to see the response.");
                        refreshQuestionsList();
                    } else {
                        logger.warn("AI failed to generate answer");
                        showError("AI Unavailable",
                                "Unable to generate AI answer at the moment.\n\nPlease try again later or wait for an expert response.");
                    }
                });
            } catch (Exception ex) {
                logger.error("Error generating AI answer", ex);
                javafx.application.Platform.runLater(() -> {
                    timeline.stop();
                    loadingDialog.close();
                    showError("Error", "Failed to generate AI answer: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openMessageDialog(Long expertId, String expertName, Long questionId, Long expertAnswerId) {
        logger.info("Opening message dialog for answer {} with expert: {} (ID: {})", expertAnswerId, expertName,
                expertId);

        String conversationId = Message.generateAnswerConversationId(currentUser.getId(), expertAnswerId);

        // Create messaging dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Message " + expertName);
        dialog.setHeaderText("Send a message to " + expertName);

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));
        content.setPrefWidth(500);

        // Recent messages area
        Label historyLabel = new Label("Recent Messages:");
        historyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        ScrollPane messagesScrollPane = new ScrollPane();
        messagesScrollPane.setPrefHeight(250);
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setStyle("-fx-background: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        VBox messagesContainer = new VBox(8);
        messagesContainer.setPadding(new javafx.geometry.Insets(10));
        messagesScrollPane.setContent(messagesContainer);

        // Load recent messages
        try {
            List<Message> messages = messageRepository.getConversationMessages(conversationId);
            if (messages.isEmpty()) {
                Label noMessagesLabel = new Label("No messages yet. Start the conversation!");
                noMessagesLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
                messagesContainer.getChildren().add(noMessagesLabel);
            } else {
                // Show last 10 messages
                List<Message> recentMessages = messages.size() > 10
                        ? messages.subList(messages.size() - 10, messages.size())
                        : messages;

                for (Message msg : recentMessages) {
                    boolean isFarmer = msg.getSenderType() == Message.SenderType.FARMER;

                    HBox messageBox = new HBox(10);
                    messageBox.setAlignment(
                            isFarmer ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

                    VBox messageBubble = new VBox(3);
                    messageBubble.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
                    messageBubble.setMaxWidth(350);
                    messageBubble.setStyle(isFarmer
                            ? "-fx-background-color: #DCF8C6; -fx-background-radius: 10; -fx-border-radius: 10;"
                            : "-fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10;");

                    // Sender name
                    Label senderLabel = new Label(
                            msg.getSenderName() != null ? msg.getSenderName() : (isFarmer ? "You" : expertName));
                    senderLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; "
                            + (isFarmer ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #1976D2;"));

                    Label messageText = new Label(msg.getMessageText());
                    messageText.setWrapText(true);
                    messageText.setStyle("-fx-font-size: 13px;");

                    String timeText = msg.getSentDate() != null
                            ? msg.getSentDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a"))
                            : "Just now";
                    Label timeLabel = new Label(timeText);
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

                    messageBubble.getChildren().addAll(senderLabel, messageText, timeLabel);
                    messageBox.getChildren().add(messageBubble);
                    messagesContainer.getChildren().add(messageBox);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading conversation messages", e);
            Label errorLabel = new Label("Could not load messages");
            errorLabel.setStyle("-fx-text-fill: red;");
            messagesContainer.getChildren().add(errorLabel);
        }

        // New message area
        Label newMessageLabel = new Label("New Message:");
        newMessageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Type your message here...");
        messageInput.setPrefRowCount(3);
        messageInput.setWrapText(true);
        messageInput.setStyle("-fx-border-color: #2196F3; -fx-border-radius: 5; -fx-background-radius: 5;");

        content.getChildren().addAll(historyLabel, messagesScrollPane, newMessageLabel, messageInput);
        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType sendButtonType = new ButtonType("Send Message", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, cancelButtonType);

        Button sendButton = (Button) dialog.getDialogPane().lookupButton(sendButtonType);
        sendButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // Handle send
        sendButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String messageText = messageInput.getText().trim();
            if (messageText.isEmpty()) {
                event.consume();
                showError("Empty Message", "Please enter a message before sending.");
                return;
            }

            try {
                logger.info("Attempting to send message. Text: '{}', Expert: {}, Conversation: {}",
                        messageText, expertId, conversationId);

                Message newMessage = new Message();
                newMessage.setConversationId(conversationId);
                newMessage.setFarmerId(currentUser.getId());
                newMessage.setExpertId(expertId);
                newMessage.setQuestionId(questionId);
                newMessage.setExpertAnswerId(expertAnswerId);
                newMessage.setSenderType(Message.SenderType.FARMER);
                newMessage.setSenderName(currentUser.getFullName());
                newMessage.setMessageText(messageText);
                newMessage.setSentDate(java.time.LocalDateTime.now());
                newMessage.setIsRead(false);

                Message savedMessage = messageRepository.save(newMessage);

                if (savedMessage == null) {
                    logger.error("Message save returned null!");
                    event.consume();
                    showError("Send Failed", "Failed to save message to database.");
                    return;
                }

                logger.info("Message saved successfully with ID: {}", savedMessage.getId());

                // Clear input field
                messageInput.clear();

                // Reload messages to show the new message
                messagesContainer.getChildren().clear();
                List<Message> messages = messageRepository.getConversationMessages(conversationId);
                logger.info("Reloaded {} messages for conversation", messages.size());

                if (messages.isEmpty()) {
                    Label noMessagesLabel = new Label("No messages yet. Start the conversation!");
                    noMessagesLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
                    messagesContainer.getChildren().add(noMessagesLabel);
                } else {
                    // Show last 10 messages
                    List<Message> recentMessages = messages.size() > 10
                            ? messages.subList(messages.size() - 10, messages.size())
                            : messages;

                    for (Message msg : recentMessages) {
                        boolean isFarmer = msg.getSenderType() == Message.SenderType.FARMER;

                        HBox messageBox = new HBox(10);
                        messageBox.setAlignment(
                                isFarmer ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

                        VBox messageBubble = new VBox(3);
                        messageBubble.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
                        messageBubble.setMaxWidth(350);
                        messageBubble.setStyle(isFarmer
                                ? "-fx-background-color: #DCF8C6; -fx-background-radius: 10; -fx-border-radius: 10;"
                                : "-fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10;");

                        // Sender name
                        Label senderLabel = new Label(
                                msg.getSenderName() != null ? msg.getSenderName() : (isFarmer ? "You" : expertName));
                        senderLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; "
                                + (isFarmer ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #1976D2;"));

                        Label msgText = new Label(msg.getMessageText());
                        msgText.setWrapText(true);
                        msgText.setStyle("-fx-font-size: 13px;");

                        String timeText = msg.getSentDate() != null
                                ? msg.getSentDate()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a"))
                                : "Just now";
                        Label timeLabel = new Label(timeText);
                        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

                        messageBubble.getChildren().addAll(senderLabel, msgText, timeLabel);
                        messageBox.getChildren().add(messageBubble);
                        messagesContainer.getChildren().add(messageBox);
                    }
                }

                // Scroll to bottom
                javafx.application.Platform.runLater(() -> {
                    messagesScrollPane.setVvalue(1.0);
                });

                // Update the Messages tab with this conversation
                updateMessagesTab(expertId, expertName, questionId, expertAnswerId);

                // Don't close the dialog - prevent the event
                event.consume();

            } catch (Exception e) {
                event.consume();
                logger.error("Error sending message", e);
                e.printStackTrace();
                showError("Send Failed", "Could not send message: " + e.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private void updateMessagesTab(Long expertId, String expertName, Long questionId, Long expertAnswerId) {
        if (mainTabPane == null || messagesTab == null) {
            logger.warn("Cannot update Messages tab - references not set");
            return;
        }

        try {
            // Get the MessagesController from the tab
            MessagesController messagesController = (MessagesController) messagesTab.getContent().getUserData();

            if (messagesController != null) {
                // Switch to Messages tab
                javafx.application.Platform.runLater(() -> {
                    mainTabPane.getSelectionModel().select(messagesTab);

                    // Open the specific conversation
                    messagesController.openAnswerChat(currentUser.getId(), currentUser.getFullName(), questionId,
                            expertAnswerId);
                });

                logger.info("Switched to Messages tab and opened conversation with expert: {}", expertName);
            } else {
                logger.warn("MessagesController not found in tab userData");
            }
        } catch (Exception e) {
            logger.error("Error updating Messages tab", e);
        }
    }

    private void openRatingDialog(Long expertId, String expertName, Long expertAnswerId) {
        logger.info("Opening rating dialog for expert: {} (ID: {})", expertName, expertId);

        // Check if farmer already rated this answer
        Rating existingRating = ratingRepository.getRatingByFarmerAndAnswer(currentUser.getId(), expertAnswerId);

        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Rate Expert Answer");
        dialog.setHeaderText(existingRating != null
                ? "Update your rating for " + expertName
                : "Rate the answer from " + expertName);

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        Label instructionLabel = new Label("How helpful was this answer?");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Star rating selector
        HBox starsBox = new HBox(10);
        starsBox.setAlignment(Pos.CENTER);
        ToggleGroup ratingGroup = new ToggleGroup();

        final RadioButton[] starButtons = new RadioButton[5];
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            RadioButton starButton = new RadioButton();
            starButton.setToggleGroup(ratingGroup);
            starButton.setUserData(rating);

            // Create star label
            Label starLabel = new Label("⭐".repeat(rating));
            starLabel.setStyle("-fx-font-size: 20px; -fx-cursor: hand;");

            VBox starContainer = new VBox(5);
            starContainer.setAlignment(Pos.CENTER);
            starContainer.getChildren().addAll(starLabel, starButton);
            starContainer.setOnMouseClicked(e -> starButton.setSelected(true));

            starsBox.getChildren().add(starContainer);
            starButtons[i] = starButton;
        }

        // Set existing rating if any
        if (existingRating != null) {
            starButtons[existingRating.getRating() - 1].setSelected(true);
        }

        // Optional comment
        Label commentLabel = new Label("Optional feedback:");
        commentLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your thoughts about this answer (optional)");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        if (existingRating != null && existingRating.getComment() != null) {
            commentArea.setText(existingRating.getComment());
        }

        content.getChildren().addAll(instructionLabel, starsBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType submitButtonType = new ButtonType(
                existingRating != null ? "Update Rating" : "Submit Rating",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);

        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

        // Disable submit button if no rating selected
        submitButton.setDisable(existingRating == null);
        ratingGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            submitButton.setDisable(newVal == null);
        });

        // Handle submit
        submitButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Toggle selectedToggle = ratingGroup.getSelectedToggle();
            if (selectedToggle == null) {
                event.consume();
                showError("No Rating", "Please select a star rating before submitting.");
                return;
            }

            int ratingValue = (Integer) selectedToggle.getUserData();
            String comment = commentArea.getText().trim();

            try {
                Rating rating = new Rating();
                rating.setExpertId(expertId);
                rating.setFarmerId(currentUser.getId());
                rating.setExpertAnswerId(expertAnswerId);
                rating.setRating(ratingValue);
                if (!comment.isEmpty()) {
                    rating.setComment(comment);
                }

                Rating savedRating = ratingRepository.save(rating);
                if (savedRating != null) {
                    logger.info("Rating saved: {} stars for expert {}", ratingValue, expertId);
                    javafx.application.Platform.runLater(() -> {
                        showInfo("Rating Submitted",
                                "Thank you for rating " + expertName + "!\n\n" +
                                        "Your " + ratingValue + " star rating has been recorded.");
                        refreshQuestionsList(); // Refresh to show updated rating
                    });
                } else {
                    event.consume();
                    showError("Save Failed", "Could not save your rating. Please try again.");
                }
            } catch (Exception e) {
                event.consume();
                logger.error("Error saving rating", e);
                showError("Error", "Failed to save rating: " + e.getMessage());
            }
        });

        dialog.showAndWait();
    }
}
