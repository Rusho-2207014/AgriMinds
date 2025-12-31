package com.agriminds.controller;

import com.agriminds.model.Expert;
import com.agriminds.model.ExpertAnswer;
import com.agriminds.model.Message;
import com.agriminds.model.Question;
import com.agriminds.model.Rating;
import com.agriminds.repository.ExpertAnswerRepository;
import com.agriminds.repository.MessageRepository;
import com.agriminds.repository.QuestionRepository;
import com.agriminds.repository.RatingRepository;
import com.agriminds.service.CertificateGenerator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ExpertController {

    private static final Logger logger = LoggerFactory.getLogger(ExpertController.class);

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private VBox questionsContainer;
    @FXML
    private VBox expertAnswersContainer;
    @FXML
    private VBox myAnswersContainer;
    @FXML
    private VBox myRatingsContainer;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab messagesTab;

    private Stage primaryStage;
    private Expert currentExpert;
    private QuestionRepository questionRepository;
    private ExpertAnswerRepository expertAnswerRepository;
    private MessageRepository messageRepository;
    private RatingRepository ratingRepository;

    public ExpertController() {
        this.questionRepository = new QuestionRepository();
        this.expertAnswerRepository = new ExpertAnswerRepository();
        this.messageRepository = new MessageRepository();
        this.ratingRepository = new RatingRepository();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setCurrentExpert(Expert expert) {
        this.currentExpert = expert;
        updateUI();

        // Reload messages tab now that we have the expert
        if (messagesTab != null) {
            loadMessagesTab();
        }
    }

    @FXML
    public void initialize() {
        statusFilterCombo.getItems().addAll("All Questions", "Unanswered", "Answered");
        statusFilterCombo.setValue("Unanswered");
        statusFilterCombo.setOnAction(e -> refreshQuestions());

        // Load messages tab if it exists
        if (messagesTab != null) {
            loadMessagesTab();
        }

        // Add listener to reload ratings and answers when tabs are selected
        if (mainTabPane != null) {
            System.out.println(">>> Tab listener registered successfully");
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                System.out.println(">>> Tab changed to: " + (newTab != null ? newTab.getText() : "null"));
                if (newTab != null) {
                    if (newTab.getText().contains("My Ratings")) {
                        System.out.println(">>> MY RATINGS TAB CLICKED!");
                        logger.info("My Ratings tab selected, loading ratings...");
                        loadMyRatings();
                    } else if (newTab.getText().contains("My Answers")) {
                        System.out.println(">>> MY ANSWERS TAB CLICKED!");
                        logger.info("My Answers tab selected, reloading answers...");
                        loadMyAnswers();
                    }
                }
            });
        } else {
            System.out.println(">>> WARNING: mainTabPane is NULL!");
        }
    }

    private void updateUI() {
        if (currentExpert != null && welcomeLabel != null) {
            // Get expert's rating
            Double avgRating = ratingRepository.getAverageRating(currentExpert.getId());
            int ratingCount = ratingRepository.getRatingCount(currentExpert.getId());
            String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);

            welcomeLabel.setText("Welcome, " + currentExpert.getFullName() + " " + ratingDisplay);
            refreshQuestions();
            loadExpertAnswers();
            loadMyAnswers();
            loadMyRatings();
        }
    }

    @FXML
    private void refreshQuestions() {
        questionsContainer.getChildren().clear();

        List<Question> allQuestions = questionRepository.getAllQuestions();

        String filter = statusFilterCombo.getValue();
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(q -> {
                    if ("Unanswered".equals(filter)) {
                        return q.getAnswerText() == null || q.getAnswerText().isEmpty();
                    } else if ("Answered".equals(filter)) {
                        return q.getAnswerText() != null && !q.getAnswerText().isEmpty();
                    }
                    return true;
                })
                .toList();

        // Count questions where the current expert hasn't answered yet
        int pendingCount = (int) allQuestions.stream()
                .filter(q -> !expertAnswerRepository.hasExpertAnswered(q.getId(), currentExpert.getId()))
                .count();

        statsLabel.setText("Pending Questions: " + pendingCount);

        if (filteredQuestions.isEmpty()) {
            Label noQuestions = new Label("No questions to display");
            noQuestions.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            questionsContainer.getChildren().add(noQuestions);
            return;
        }

        for (Question question : filteredQuestions) {
            VBox questionCard = createQuestionCard(question);
            questionsContainer.getChildren().add(questionCard);
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label categoryBadge = new Label(question.getCategory());
        categoryBadge.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; " +
                "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Check for answers from new expert_answers table
        int expertAnswerCount = expertAnswerRepository.getAnswerCount(question.getId());
        boolean hasAIAnswer = question.getAiAnswerText() != null && !question.getAiAnswerText().isEmpty();

        Label statusBadge;
        String badgeStyle;
        if (expertAnswerCount > 0) {
            statusBadge = new Label(expertAnswerCount + " Expert Answer" + (expertAnswerCount > 1 ? "s" : ""));
            badgeStyle = "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32;";
        } else if (hasAIAnswer) {
            statusBadge = new Label("AI Answered");
            badgeStyle = "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
        } else {
            statusBadge = new Label("Open");
            badgeStyle = "-fx-background-color: #FFECB3; -fx-text-fill: #F57C00;";
        }
        statusBadge.setStyle(badgeStyle
                + " -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label(question.getAskedDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        header.getChildren().addAll(categoryBadge, statusBadge, spacer, dateLabel);

        Label questionText = new Label(question.getQuestionText());
        questionText.setWrapText(true);
        questionText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-padding: 5 0;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Check if current expert has already answered this question
        boolean hasCurrentExpertAnswered = expertAnswerRepository.hasExpertAnswered(question.getId(),
                currentExpert.getId());

        // Show answer button if current expert hasn't answered yet
        if (!hasCurrentExpertAnswered) {
            String buttonText;
            if (expertAnswerCount > 0 && hasAIAnswer) {
                buttonText = "✍ Add Your Answer";
            } else if (expertAnswerCount > 0) {
                buttonText = "✍ Add Another Answer";
            } else if (hasAIAnswer) {
                buttonText = "✍ Add Expert Answer";
            } else {
                buttonText = "✍ Answer This Question";
            }

            Button answerBtn = new Button(buttonText);
            answerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 8 15; -fx-cursor: hand;");
            answerBtn.setOnAction(e -> showAnswerDialog(question));
            buttonBox.getChildren().add(answerBtn);
        }

        // Always show view button if any answer exists (AI or expert)
        if (hasAIAnswer || expertAnswerCount > 0) {
            String viewText = "👁 View ";
            if (hasAIAnswer && expertAnswerCount > 0) {
                viewText += "All Answers";
            } else if (expertAnswerCount > 1) {
                viewText += expertAnswerCount + " Answers";
            } else {
                viewText += "Answer";
            }

            Button viewAnswerBtn = new Button(viewText);
            viewAnswerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 8 15; -fx-cursor: hand;");
            viewAnswerBtn.setOnAction(e -> showViewAnswerDialog(question));
            buttonBox.getChildren().add(viewAnswerBtn);
        }

        card.getChildren().addAll(header, questionText, buttonBox);
        return card;
    }

    private void showAnswerDialog(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Answer Question");
        dialog.setHeaderText("Question: " + question.getQuestionText());

        ButtonType submitButtonType = new ButtonType("Submit Answer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Show AI answer if it exists
        boolean hasAIAnswer = question.getAiAnswerText() != null && !question.getAiAnswerText().isEmpty();
        if (hasAIAnswer) {
            VBox aiAnswerBox = new VBox(8);
            aiAnswerBox.setPadding(new Insets(10));
            aiAnswerBox.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");

            Label aiLabel = new Label("🤖 AI has already answered this question:");
            aiLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

            TextArea aiAnswerArea = new TextArea(question.getAiAnswerText());
            aiAnswerArea.setEditable(false);
            aiAnswerArea.setPrefRowCount(4);
            aiAnswerArea.setWrapText(true);
            aiAnswerArea.setStyle("-fx-control-inner-background: #E3F2FD;");

            aiAnswerBox.getChildren().addAll(aiLabel, aiAnswerArea);
            content.getChildren().add(aiAnswerBox);

            Separator separator = new Separator();
            content.getChildren().add(separator);
        }

        Label instruction = new Label(hasAIAnswer ? "Add your expert answer:" : "Your answer:");
        instruction.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your detailed answer here...");
        answerArea.setPrefRowCount(6);
        answerArea.setPrefWidth(500);
        answerArea.setWrapText(true);

        content.getChildren().addAll(instruction, answerArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefHeight(hasAIAnswer ? 550 : 350);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return answerArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            if (answer != null && !answer.trim().isEmpty()) {
                // Check if this expert has already answered
                if (expertAnswerRepository.hasExpertAnswered(question.getId(), currentExpert.getId())) {
                    Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                    warningAlert.setTitle("Already Answered");
                    warningAlert.setHeaderText(null);
                    warningAlert.setContentText("You have already answered this question!");
                    warningAlert.showAndWait();
                    return;
                }

                // Save expert answer to new table
                ExpertAnswer expertAnswer = new ExpertAnswer(
                        question.getId(),
                        currentExpert.getId(),
                        currentExpert.getFullName(),
                        answer.trim());

                ExpertAnswer saved = expertAnswerRepository.save(expertAnswer);

                if (saved != null) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Answer submitted successfully!");
                    successAlert.showAndWait();

                    refreshQuestions();
                    loadMyAnswers();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to submit answer. Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void showViewAnswerDialog(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Answer Details");
        dialog.setHeaderText("Question: " + question.getQuestionText());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefWidth(700);
        scrollPane.setPrefHeight(500);
        scrollPane.setFitToWidth(true);

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(10));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        boolean hasAIAnswer = question.getAiAnswerText() != null && !question.getAiAnswerText().isEmpty();

        // Get all expert answers from new table
        List<ExpertAnswer> expertAnswers = expertAnswerRepository.getAnswersByQuestionId(question.getId());

        // AI Answer section
        if (hasAIAnswer) {
            VBox aiBox = new VBox(8);
            aiBox.setPadding(new Insets(12));
            aiBox.setStyle(
                    "-fx-background-color: #E3F2FD; -fx-background-radius: 8; -fx-border-color: #2196F3; -fx-border-width: 1; -fx-border-radius: 8;");

            Label aiTitle = new Label("🤖 AI ANSWER");
            aiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

            Label aiInfo = new Label("By: AI Agricultural Advisor" +
                    (question.getAiAnsweredDate() != null ? " • " + question.getAiAnsweredDate().format(formatter)
                            : ""));
            aiInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            Label aiAnswer = new Label(question.getAiAnswerText());
            aiAnswer.setWrapText(true);
            aiAnswer.setStyle("-fx-font-size: 13px;");

            aiBox.getChildren().addAll(aiTitle, aiInfo, aiAnswer);
            contentBox.getChildren().add(aiBox);
        }

        // Expert answers section
        if (!expertAnswers.isEmpty()) {
            for (int i = 0; i < expertAnswers.size(); i++) {
                ExpertAnswer ea = expertAnswers.get(i);

                VBox expertBox = new VBox(8);
                expertBox.setPadding(new Insets(12));
                expertBox.setStyle(
                        "-fx-background-color: #F1F8E9; -fx-background-radius: 8; -fx-border-color: #8BC34A; -fx-border-width: 1; -fx-border-radius: 8;");

                Label expertTitle = new Label("👨‍🌾 EXPERT ANSWER #" + (i + 1));
                expertTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #558B2F;");

                // Create HBox for expert info and reply button
                HBox infoBox = new HBox(10);
                infoBox.setAlignment(Pos.CENTER_LEFT);

                // Get rating for this expert
                Double avgRating = ratingRepository.getAverageRating(ea.getExpertId());
                int ratingCount = ratingRepository.getRatingCount(ea.getExpertId());
                String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);

                Label expertInfo = new Label("By: " + ea.getExpertName() + " " + ratingDisplay +
                        (ea.getAnsweredDate() != null ? " • " + ea.getAnsweredDate().format(formatter) : ""));
                expertInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                HBox.setHgrow(expertInfo, Priority.ALWAYS);

                infoBox.getChildren().add(expertInfo);

                // Add Reply button only if this answer is from a different expert (not current
                // user)
                if (!ea.getExpertId().equals(currentExpert.getId())) {
                    Button replyBtn = new Button("✏️ Reply/Correct");
                    replyBtn.setStyle(
                            "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 15;");
                    replyBtn.setOnAction(e -> showReplyToExpertDialog(question, ea));
                    infoBox.getChildren().add(replyBtn);
                }

                Label expertAnswer = new Label(ea.getAnswerText());
                expertAnswer.setWrapText(true);
                expertAnswer.setStyle("-fx-font-size: 13px;");

                expertBox.getChildren().addAll(expertTitle, infoBox, expertAnswer);

                // Show replies to this answer
                List<ExpertAnswer> replies = expertAnswerRepository.getRepliesByParentAnswerId(ea.getId());
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
                                        + borderColor
                                        + "; -fx-border-width: 1; -fx-border-radius: 6; -fx-margin: 5 0 0 0;");

                        String replyTypeLabel = reply.getReplyType() != null ? reply.getReplyType().toUpperCase()
                                : "REPLY";

                        // Add acceptance status with farmer name
                        String statusText = "";
                        if (reply.getAccepted() != null) {
                            // Get farmer name from the question
                            String farmerName = question.getFarmerName();
                            if (reply.getAccepted()) {
                                statusText = " - ✓ Accepted by " + farmerName;
                            } else {
                                statusText = " - ✗ Denied by " + farmerName;
                            }
                        }

                        Label replyTitle = new Label(
                                "↳ " + replyTypeLabel + " by " + reply.getExpertName() + statusText);
                        replyTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #F57F17;");

                        Label replyDate = new Label(
                                reply.getAnsweredDate() != null ? reply.getAnsweredDate().format(formatter) : "");
                        replyDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

                        Label replyText = new Label(reply.getAnswerText());
                        replyText.setWrapText(true);
                        replyText.setStyle("-fx-font-size: 12px;");

                        replyBox.getChildren().addAll(replyTitle, replyDate, replyText);
                        expertBox.getChildren().add(replyBox);
                    }
                }

                contentBox.getChildren().add(expertBox);
            }
        }

        if (!hasAIAnswer && expertAnswers.isEmpty()) {
            Label noAnswers = new Label("No answers available yet.");
            noAnswers.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-style: italic;");
            contentBox.getChildren().add(noAnswers);
        }

        scrollPane.setContent(contentBox);
        dialog.getDialogPane().setContent(scrollPane);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        dialog.showAndWait();
    }

    /**
     * Show dialog for expert to reply to or correct another expert's answer
     */
    private void showReplyToExpertDialog(Question question, ExpertAnswer originalAnswer) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reply to Expert Answer");
        dialog.setHeaderText("Original Question: " + question.getQuestionText());

        ButtonType submitButtonType = new ButtonType("Submit Reply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);

        // Show original expert's answer
        VBox originalBox = new VBox(8);
        originalBox.setPadding(new Insets(12));
        originalBox.setStyle(
                "-fx-background-color: #F1F8E9; -fx-background-radius: 8; -fx-border-color: #8BC34A; -fx-border-width: 2; -fx-border-radius: 8;");

        Label originalTitle = new Label("📌 Original Answer by " + originalAnswer.getExpertName());
        originalTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #558B2F;");

        TextArea originalTextArea = new TextArea(originalAnswer.getAnswerText());
        originalTextArea.setEditable(false);
        originalTextArea.setWrapText(true);
        originalTextArea.setPrefRowCount(4);
        originalTextArea.setStyle("-fx-control-inner-background: #F1F8E9;");

        originalBox.getChildren().addAll(originalTitle, originalTextArea);
        content.getChildren().add(originalBox);

        // Reply type selection
        Label typeLabel = new Label("Select reply type:");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<String> replyTypeCombo = new ComboBox<>();
        replyTypeCombo.getItems().addAll("correction", "addition", "reply");
        replyTypeCombo.setValue("correction");
        replyTypeCombo.setPrefWidth(200);

        HBox typeBox = new HBox(10, typeLabel, replyTypeCombo);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(typeBox);

        // Reply text area
        Label replyLabel = new Label("Your reply/correction:");
        replyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply, correction, or additional information...");
        replyArea.setPrefRowCount(6);
        replyArea.setWrapText(true);

        content.getChildren().addAll(replyLabel, replyArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefHeight(550);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return replyArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(replyText -> {
            if (replyText != null && !replyText.trim().isEmpty()) {
                // Check if expert already has a reply to this answer
                ExpertAnswer existing = expertAnswerRepository.getExistingReply(originalAnswer.getId(),
                        currentExpert.getId());

                boolean success;
                String action;

                if (existing != null) {
                    // Update existing reply
                    success = expertAnswerRepository.updateReply(existing.getId(), replyText.trim(),
                            replyTypeCombo.getValue());
                    action = "updated";
                } else {
                    // Create new reply answer
                    ExpertAnswer reply = new ExpertAnswer();
                    reply.setQuestionId(question.getId());
                    reply.setExpertId(currentExpert.getId());
                    reply.setExpertName(currentExpert.getFullName());
                    reply.setParentAnswerId(originalAnswer.getId());
                    reply.setReplyType(replyTypeCombo.getValue());
                    reply.setAnswerText(replyText.trim());
                    reply.setAnsweredDate(LocalDateTime.now());

                    ExpertAnswer saved = expertAnswerRepository.save(reply);
                    success = (saved != null);
                    action = "posted";
                }

                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Your " + replyTypeCombo.getValue()
                            + " has been " + action + " successfully!\n\n" +
                            "The farmer will see both the original answer and your " + replyTypeCombo.getValue() + ".");
                    successAlert.showAndWait();

                    // Refresh the question list to show the updated status
                    refreshQuestions();
                    loadMyAnswers();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to " + action + " your reply. Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void loadExpertAnswers() {
        expertAnswersContainer.getChildren().clear();

        // Get all questions that have expert answers
        List<Question> allQuestions = questionRepository.getAllQuestions();
        List<Question> questionsWithExpertAnswers = allQuestions.stream()
                .filter(q -> {
                    int answerCount = expertAnswerRepository.getAnswerCount(q.getId());
                    return answerCount > 0;
                })
                .toList();

        if (questionsWithExpertAnswers.isEmpty()) {
            Label noAnswers = new Label("No expert answers in the community yet");
            noAnswers.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            expertAnswersContainer.getChildren().add(noAnswers);
            return;
        }

        for (Question question : questionsWithExpertAnswers) {
            VBox answerCard = createAnswerCard(question);
            expertAnswersContainer.getChildren().add(answerCard);
        }
    }

    private void loadMyAnswers() {
        myAnswersContainer.getChildren().clear();

        // Get all answers by current expert from new table
        List<ExpertAnswer> myAnswers = expertAnswerRepository.getAnswersByExpertId(currentExpert.getId());

        if (myAnswers.isEmpty()) {
            Label noAnswers = new Label("You haven't answered any questions yet");
            noAnswers.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            myAnswersContainer.getChildren().add(noAnswers);
            return;
        }

        for (ExpertAnswer expertAnswer : myAnswers) {
            // Get the question for this answer
            Question question = questionRepository.getQuestionById(expertAnswer.getQuestionId());
            if (question != null) {
                VBox answerCard = createMyAnswerCard(question, expertAnswer);
                myAnswersContainer.getChildren().add(answerCard);
            }
        }
    }

    private VBox createMyAnswerCard(Question question, ExpertAnswer expertAnswer) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        Label questionLabel = new Label("Q: " + question.getQuestionText());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label answerLabel = new Label("A: " + expertAnswer.getAnswerText());
        answerLabel.setWrapText(true);
        answerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555; -fx-padding: 5 0 0 15;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        Label dateLabel = new Label("Answered on: " + expertAnswer.getAnsweredDate().format(formatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2E7D32; -fx-font-style: italic;");

        // Check if farmer has replied to this answer
        logger.info("Checking for replies to answer ID: {} for question: {}", expertAnswer.getId(), question.getId());
        boolean hasFarmerReplies = messageRepository.hasFarmerRepliesForAnswer(expertAnswer.getId());
        boolean hasExpertReplied = messageRepository.hasExpertRepliedToAnswer(expertAnswer.getId(),
                currentExpert.getId());

        logger.info("Answer {}: hasFarmerReplies={}, hasExpertReplied={}", expertAnswer.getId(), hasFarmerReplies,
                hasExpertReplied);

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.getChildren().add(dateLabel);

        if (hasFarmerReplies) {
            Button replyButton;
            if (hasExpertReplied) {
                // Expert has already replied - show "View Answer" button
                replyButton = new Button("💬 View Answer");
                replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
            } else {
                // Farmer replied but expert hasn't - show "Answer Reply" button
                replyButton = new Button("✉️ Answer Reply");
                replyButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
            }

            replyButton.setOnAction(e -> openAnswerMessageDialog(expertAnswer, question));
            bottomBox.getChildren().add(replyButton);
        }

        card.getChildren().addAll(questionLabel, answerLabel, bottomBox);
        return card;
    }

    /**
     * Open message dialog for a specific answer conversation
     */
    private void openAnswerMessageDialog(ExpertAnswer expertAnswer, Question question) {
        // Get farmer ID from the first message in this answer's conversation
        String conversationId = "farmer_*_answer_" + expertAnswer.getId();

        // Get messages for this answer
        List<Message> messages = messageRepository.getConversationMessages(
                Message.generateAnswerConversationId(question.getFarmerId(), expertAnswer.getId()));

        if (messages.isEmpty()) {
            // No messages found, this shouldn't happen but handle gracefully
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Messages");
            alert.setHeaderText("No messages found for this answer");
            alert.showAndWait();
            return;
        }

        // Get farmer ID from the first message
        Long farmerId = messages.get(0).getFarmerId();
        String farmerName = messages.get(0).getSenderType() == Message.SenderType.FARMER
                ? messages.get(0).getSenderName()
                : "Farmer";

        // Switch to messages tab and open this conversation
        mainTabPane.getSelectionModel().select(messagesTab);

        // Load messages tab if not already loaded
        if (messagesTab.getContent() == null) {
            loadMessagesTab();
        }

        // Get the messages controller and open the chat
        try {
            MessagesController messagesController = (MessagesController) messagesTab.getContent().getUserData();
            if (messagesController != null) {
                // Ensure current expert is set
                messagesController.setCurrentUser(currentExpert);
                // Open the specific answer conversation
                messagesController.openAnswerChat(farmerId, farmerName, question.getId(), expertAnswer.getId());
            } else {
                logger.error("MessagesController is null - tab may not be loaded properly");
                // Reload the tab
                loadMessagesTab();
                messagesController = (MessagesController) messagesTab.getContent().getUserData();
                if (messagesController != null) {
                    messagesController.setCurrentUser(currentExpert);
                    messagesController.openAnswerChat(farmerId, farmerName, question.getId(), expertAnswer.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error opening message dialog for answer", e);
            e.printStackTrace();
        }
    }

    private VBox createAnswerCard(Question question) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        Label questionLabel = new Label("Q: " + question.getQuestionText());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        card.getChildren().add(questionLabel);

        // Get all expert answers for this question (excluding replies)
        List<ExpertAnswer> expertAnswers = expertAnswerRepository.getAnswersByQuestionId(question.getId());

        if (!expertAnswers.isEmpty()) {
            Label answerCountLabel = new Label(
                    expertAnswers.size() + " Expert Answer" + (expertAnswers.size() > 1 ? "s" : ""));
            answerCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
            card.getChildren().add(answerCountLabel);

            // Show first few answers as preview
            int previewCount = Math.min(2, expertAnswers.size());
            for (int i = 0; i < previewCount; i++) {
                ExpertAnswer answer = expertAnswers.get(i);

                VBox answerBox = new VBox(5);
                answerBox.setPadding(new Insets(8));
                answerBox.setStyle(
                        "-fx-background-color: #F5F5F5; -fx-background-radius: 5; -fx-border-color: #DDD; -fx-border-radius: 5; -fx-border-width: 1;");

                Label expertNameLabel = new Label("Expert: " + answer.getExpertName());
                expertNameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

                Label answerTextLabel = new Label(answer.getAnswerText().length() > 100
                        ? answer.getAnswerText().substring(0, 100) + "..."
                        : answer.getAnswerText());
                answerTextLabel.setWrapText(true);
                answerTextLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

                answerBox.getChildren().addAll(expertNameLabel, answerTextLabel);
                card.getChildren().add(answerBox);
            }

            // View all answers button
            Button viewButton = new Button("👁 View All Answers");
            viewButton.setStyle(
                    "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
            viewButton.setOnAction(e -> showViewAnswerDialog(question));

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_LEFT);
            buttonBox.getChildren().add(viewButton);

            card.getChildren().add(buttonBox);
        }

        return card;
    }

    private void loadMyRatings() {
        System.out.println(">>> loadMyRatings() METHOD CALLED!");

        if (myRatingsContainer == null || currentExpert == null) {
            System.out.println(">>> ERROR: container=" + myRatingsContainer + ", expert=" + currentExpert);
            logger.warn("Cannot load ratings: container={}, expert={}", myRatingsContainer, currentExpert);
            return;
        }

        System.out.println("========================================");
        System.out.println("Loading ratings for expert ID: " + currentExpert.getId());
        System.out.println("Expert name: " + currentExpert.getFullName());
        System.out.println("========================================");

        logger.info("========================================");
        logger.info("Loading ratings for expert ID: {}", currentExpert.getId());
        logger.info("Expert name: {}", currentExpert.getFullName());
        logger.info("========================================");

        myRatingsContainer.getChildren().clear();

        // First check simple ratings count
        List<Rating> simpleRatings = ratingRepository.getRatingsByExpert(currentExpert.getId());
        System.out.println("Simple ratings count: " + simpleRatings.size());
        logger.info("Simple ratings count: {}", simpleRatings.size());

        List<RatingRepository.RatingDetails> ratings = ratingRepository.getRatingsWithDetails(currentExpert.getId());
        System.out.println("Retrieved rating details: " + ratings.size());
        logger.info("Retrieved {} rating details", ratings.size());

        if (!simpleRatings.isEmpty() && ratings.isEmpty()) {
            System.out.println(
                    "ERROR: MISMATCH - Found " + simpleRatings.size() + " simple ratings but 0 detailed ratings!");
            logger.error("MISMATCH: Found {} simple ratings but 0 detailed ratings - SQL JOIN issue!",
                    simpleRatings.size());
        }

        if (ratings.isEmpty()) {
            Label emptyLabel = new Label("📭 No ratings received yet");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 50;");
            myRatingsContainer.getChildren().add(emptyLabel);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        // === AVERAGE RATING SECTION ===
        Double expertAvgRating = ratingRepository.getAverageRating(currentExpert.getId());
        int totalRatings = ratingRepository.getRatingCount(currentExpert.getId());
        int ratedAnswersCount = ratingRepository.getRatedAnswersCount(currentExpert.getId());
        int answeredQuestionsCount = expertAnswerRepository.getAnsweredQuestionsCount(currentExpert.getId());
        int repliesCount = expertAnswerRepository.getRepliesCount(currentExpert.getId());

        VBox avgRatingBox = new VBox(12);
        avgRatingBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #8B5CF6 0%, #6D28D9 100%); " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        avgRatingBox.setPrefWidth(1100);
        avgRatingBox.setAlignment(Pos.CENTER);

        Label avgTitle = new Label("📊 OVERALL RATING");
        avgTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox avgInfoBox = new HBox(30);
        avgInfoBox.setAlignment(Pos.CENTER);

        // Average rating display
        VBox avgBox = new VBox(5);
        avgBox.setAlignment(Pos.CENTER);
        String avgRatingText = expertAvgRating != null ? String.format("%.2f", expertAvgRating) : "0.00";
        Label avgRatingLabel = new Label(avgRatingText);
        avgRatingLabel.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        Label avgStars = new Label("⭐".repeat(expertAvgRating != null ? (int) Math.round(expertAvgRating) : 0));
        avgStars.setStyle("-fx-font-size: 24px;");
        Label avgSubtext = new Label("Average Rating");
        avgSubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        avgBox.getChildren().addAll(avgRatingLabel, avgStars, avgSubtext);

        // Separator 1
        Region separator1 = new Region();
        separator1.setPrefWidth(2);
        separator1.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");

        // Total ratings count
        VBox countBox = new VBox(5);
        countBox.setAlignment(Pos.CENTER);
        Label countLabel = new Label(String.valueOf(totalRatings));
        countLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label countSubtext = new Label("Total Ratings");
        countSubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        countBox.getChildren().addAll(countLabel, countSubtext);

        // Separator 2
        Region separator2 = new Region();
        separator2.setPrefWidth(2);
        separator2.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");

        // Questions answered
        VBox answeredBox = new VBox(5);
        answeredBox.setAlignment(Pos.CENTER);
        Label answeredLabel = new Label(String.valueOf(answeredQuestionsCount));
        answeredLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label answeredSubtext = new Label("Questions Answered");
        answeredSubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        answeredBox.getChildren().addAll(answeredLabel, answeredSubtext);

        // Separator 3
        Region separator3 = new Region();
        separator3.setPrefWidth(2);
        separator3.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");

        // Rated answers
        VBox ratedBox = new VBox(5);
        ratedBox.setAlignment(Pos.CENTER);
        Label ratedLabel = new Label(String.valueOf(ratedAnswersCount));
        ratedLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label ratedSubtext = new Label("Rated Answers");
        ratedSubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        ratedBox.getChildren().addAll(ratedLabel, ratedSubtext);

        // Separator 4
        Region separator4 = new Region();
        separator4.setPrefWidth(2);
        separator4.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");

        // Corrections/Additions/Replies
        VBox repliesBox = new VBox(5);
        repliesBox.setAlignment(Pos.CENTER);
        Label repliesLabel = new Label(String.valueOf(repliesCount));
        repliesLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label repliesSubtext = new Label("Corrections/Additions/Replies");
        repliesSubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        repliesBox.getChildren().addAll(repliesLabel, repliesSubtext);

        avgInfoBox.getChildren().addAll(avgBox, separator1, countBox, separator2, answeredBox, separator3, ratedBox,
                separator4, repliesBox);
        avgRatingBox.getChildren().addAll(avgTitle, avgInfoBox);
        myRatingsContainer.getChildren().add(avgRatingBox);

        // === LATEST RATING SUMMARY SECTION ===
        RatingRepository.RatingDetails latestRating = ratings.get(0);
        VBox latestRatingBox = new VBox(15);
        latestRatingBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        latestRatingBox.setPrefWidth(1100);

        Label latestTitle = new Label("⚡ LATEST RATING");
        latestTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox latestInfoBox = new HBox(20);
        latestInfoBox.setAlignment(Pos.CENTER_LEFT);

        Label latestStars = new Label("⭐".repeat(latestRating.getRating()));
        latestStars.setStyle("-fx-font-size: 32px;");

        VBox latestDetails = new VBox(5);
        Label latestScore = new Label(latestRating.getRating() + " out of 5 stars");
        latestScore.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");

        Label latestFarmer = new Label("From: " + latestRating.getFarmerName());
        latestFarmer.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Label latestDate = new Label(latestRating.getRatedDate().format(formatter));
        latestDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E0E0;");

        latestDetails.getChildren().addAll(latestScore, latestFarmer, latestDate);
        latestInfoBox.getChildren().addAll(latestStars, latestDetails);

        latestRatingBox.getChildren().addAll(latestTitle, latestInfoBox);
        myRatingsContainer.getChildren().add(latestRatingBox);

        // === CERTIFICATE DOWNLOAD BUTTON (ALWAYS VISIBLE) ===
        // expertAvgRating already calculated above
        System.out.println("========================================");
        System.out.println("CERTIFICATE SECTION DEBUG:");
        System.out.println("Expert Average Rating: " + expertAvgRating);
        System.out.println("Average Rating Text: " + avgRatingText);
        System.out.println("========================================");

        boolean qualifiesForCertificate = expertAvgRating != null && expertAvgRating >= 4.8;
        System.out.println("Qualifies for certificate: " + qualifiesForCertificate);

        // Get the best (highest) rating for certificate generation
        RatingRepository.RatingDetails bestRating = qualifiesForCertificate
                ? ratings.stream().max((r1, r2) -> Integer.compare(r1.getRating(), r2.getRating())).orElse(null)
                : null;
        System.out.println("Best rating: " + (bestRating != null ? bestRating.getRating() : "null"));
        System.out.println("========================================");

        VBox certificateSection = new VBox(10);
        certificateSection.setAlignment(Pos.CENTER);
        certificateSection.setPadding(new Insets(20, 0, 20, 0));

        Button certificateButton = new Button(
                qualifiesForCertificate ? "📜 Download Excellence Certificate"
                        : "🏆 Reach 4.8 Average Rating for Certificate");

        if (qualifiesForCertificate) {
            certificateButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #FFD700, #DAA520); " +
                            "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                            "-fx-padding: 15 30; -fx-background-radius: 25; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);");

            certificateButton.setOnMouseEntered(e -> certificateButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #FFC700, #C9A020); " +
                            "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                            "-fx-padding: 15 30; -fx-background-radius: 25; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);"));

            certificateButton.setOnMouseExited(e -> certificateButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #FFD700, #DAA520); " +
                            "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                            "-fx-padding: 15 30; -fx-background-radius: 25; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"));

            RatingRepository.RatingDetails finalBestRating = bestRating;
            certificateButton.setOnAction(e -> downloadCertificate(finalBestRating));
        } else {
            certificateButton.setStyle(
                    "-fx-background-color: #CCCCCC; " +
                            "-fx-text-fill: #666666; -fx-font-size: 16px; -fx-font-weight: bold; " +
                            "-fx-padding: 15 30; -fx-background-radius: 25; -fx-cursor: not-allowed; " +
                            "-fx-opacity: 0.7;");
            certificateButton.setDisable(true);
        }

        Label certificateInfo = new Label(
                qualifiesForCertificate
                        ? "🎉 Congratulations! Your average rating is " + avgRatingText
                                + " ★ - You qualify for a certificate!"
                        : "Current average: " + avgRatingText + " ★ - Keep providing excellent answers to reach 4.8!");
        certificateInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        certificateSection.getChildren().addAll(certificateButton, certificateInfo);
        myRatingsContainer.getChildren().add(certificateSection);

        // === SECTION TITLE FOR ALL RATINGS ===
        Label allRatingsTitle = new Label("📊 All Ratings History");
        allRatingsTitle
                .setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 20 0 10 0;");
        myRatingsContainer.getChildren().add(allRatingsTitle);

        // === ALL RATINGS LIST ===
        for (RatingRepository.RatingDetails ratingDetail : ratings) {
            boolean isFiveStar = ratingDetail.getRating() == 5;

            VBox ratingCard = new VBox(12);
            String borderColor = isFiveStar ? "#FFD700" : "#FF9800";
            String backgroundColor = isFiveStar ? "#FFFEF0" : "white";

            ratingCard.setStyle("-fx-background-color: " + backgroundColor + "; -fx-border-color: " + borderColor
                    + "; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            ratingCard.setPrefWidth(1100);

            // Rating header with stars
            HBox ratingHeader = new HBox(10);
            ratingHeader.setAlignment(Pos.CENTER_LEFT);

            Label starsLabel = new Label("⭐".repeat(ratingDetail.getRating()));
            starsLabel.setStyle("-fx-font-size: 20px;");

            Label ratingText = new Label(ratingDetail.getRating() + " out of 5 stars");
            String ratingColor = isFiveStar ? "#FFD700" : "#FF9800";
            ratingText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + ratingColor + ";");

            if (isFiveStar) {
                Label perfectBadge = new Label("🏆 PERFECT");
                perfectBadge.setStyle("-fx-background-color: #FFD700; -fx-text-fill: white; " +
                        "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                ratingHeader.getChildren().add(perfectBadge);
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label farmerLabel = new Label("Rated by: " + ratingDetail.getFarmerName());
            farmerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            Label dateLabel = new Label(ratingDetail.getRatedDate().format(formatter));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

            ratingHeader.getChildren().addAll(starsLabel, ratingText, spacer, farmerLabel, dateLabel);

            // Question
            Label questionLabel = new Label("Question: " + ratingDetail.getQuestionTitle());
            questionLabel.setWrapText(true);
            questionLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");

            // Your Answer
            Label yourAnswerTitle = new Label("Your Answer:");
            yourAnswerTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");

            Label answerLabel = new Label(ratingDetail.getAnswer());
            answerLabel.setWrapText(true);
            answerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-padding: 0 0 0 15;");

            ratingCard.getChildren().addAll(ratingHeader, questionLabel, yourAnswerTitle, answerLabel);

            // Farmer's comment if exists
            if (ratingDetail.getComment() != null && !ratingDetail.getComment().trim().isEmpty()) {
                Label commentTitle = new Label("Farmer's Feedback:");
                commentTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");

                Label commentLabel = new Label("\"" + ratingDetail.getComment() + "\"");
                commentLabel.setWrapText(true);
                commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic; " +
                        "-fx-padding: 5; -fx-background-color: #FFF3E0; -fx-background-radius: 5;");

                ratingCard.getChildren().addAll(commentTitle, commentLabel);
            }

            myRatingsContainer.getChildren().add(ratingCard);
        }

        logger.info("Loaded {} ratings for expert", ratings.size());
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            logger.error("Failed to logout", e);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About AgriMinds");
        alert.setHeaderText("Expert Dashboard");
        alert.setContentText("Help farmers by answering their questions.\nVersion 1.0");
        alert.showAndWait();
    }

    private void loadMessagesTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tabs/messages-tab.fxml"));
            Parent content = loader.load();
            messagesTab.setContent(content);

            MessagesController controller = loader.getController();
            if (currentExpert != null) {
                controller.setCurrentUser(currentExpert);
            }

            // Store controller in user data for later access
            content.setUserData(controller);
        } catch (Exception e) {
            logger.error("Failed to load messages tab", e);
            messagesTab.setContent(new Label("Error loading messages"));
        }
    }

    private void openExpertMessageDialog(Long farmerId, Long otherExpertId, String otherExpertName, String farmerName) {
        logger.info("Opening message dialog - Expert to Expert communication about farmer: {}", farmerName);

        // Note: For expert-to-expert messaging about a farmer's question, we use the
        // farmer ID to generate conversation
        String conversationId = Message.generateConversationId(farmerId, otherExpertId);

        // Create messaging dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Message " + otherExpertName);
        dialog.setHeaderText("Send a message regarding " + farmerName + "'s question");

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(500);

        // Info label
        Label infoLabel = new Label("You can discuss this farmer's question with " + otherExpertName);
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        // Recent messages area
        Label historyLabel = new Label("Recent Messages:");
        historyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        ScrollPane messagesScrollPane = new ScrollPane();
        messagesScrollPane.setPrefHeight(250);
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setStyle("-fx-background: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        VBox messagesContainer = new VBox(8);
        messagesContainer.setPadding(new Insets(10));
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
                    boolean isCurrentExpert = msg.getSenderType() == Message.SenderType.EXPERT &&
                            msg.getExpertId().equals(currentExpert.getId());

                    HBox messageBox = new HBox(10);
                    messageBox.setAlignment(isCurrentExpert ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                    VBox messageBubble = new VBox(3);
                    messageBubble.setPadding(new Insets(8, 12, 8, 12));
                    messageBubble.setMaxWidth(350);
                    messageBubble.setStyle(isCurrentExpert
                            ? "-fx-background-color: #DCF8C6; -fx-background-radius: 10; -fx-border-radius: 10;"
                            : "-fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10;");

                    // Sender name
                    String displayName = msg.getSenderName() != null ? msg.getSenderName()
                            : (msg.getSenderType() == Message.SenderType.FARMER ? farmerName
                                    : (isCurrentExpert ? "You" : otherExpertName));
                    Label senderLabel = new Label(displayName);
                    senderLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                            (isCurrentExpert ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #1976D2;"));

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
        messageInput.setStyle("-fx-border-color: #8BC34A; -fx-border-radius: 5; -fx-background-radius: 5;");

        content.getChildren().addAll(infoLabel, historyLabel, messagesScrollPane, newMessageLabel, messageInput);
        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType sendButtonType = new ButtonType("Send Message", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, cancelButtonType);

        Button sendButton = (Button) dialog.getDialogPane().lookupButton(sendButtonType);
        sendButton.setStyle("-fx-background-color: #8BC34A; -fx-text-fill: white; -fx-font-weight: bold;");

        // Handle send
        sendButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String messageText = messageInput.getText().trim();
            if (messageText.isEmpty()) {
                event.consume();
                showErrorAlert("Empty Message", "Please enter a message before sending.");
                return;
            }

            try {
                Message newMessage = new Message();
                newMessage.setConversationId(conversationId);
                newMessage.setFarmerId(farmerId);
                newMessage.setExpertId(currentExpert.getId());
                newMessage.setSenderType(Message.SenderType.EXPERT);
                newMessage.setSenderName(currentExpert.getFullName());
                newMessage.setMessageText(messageText);
                newMessage.setSentDate(java.time.LocalDateTime.now());
                newMessage.setIsRead(false);

                messageRepository.save(newMessage);
                logger.info("Message sent from expert {} to expert {} about farmer {}",
                        currentExpert.getId(), otherExpertId, farmerId);

                Platform.runLater(() -> {
                    showInfoAlert("Message Sent", "Your message has been sent!");
                });
            } catch (Exception e) {
                event.consume();
                logger.error("Error sending message", e);
                showErrorAlert("Send Failed", "Could not send message: " + e.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Download certificate for 4.8+ rating achievement
     */
    private void downloadCertificate(RatingRepository.RatingDetails ratingDetail) {
        logger.info("Generating certificate for expert with 4.8+ rating");

        // Validate rating detail
        if (ratingDetail == null) {
            logger.error("Rating detail is null");
            showErrorAlert("Error", "Cannot generate certificate: No rating information available.");
            return;
        }

        // Get statistics
        Double avgRating = ratingRepository.getAverageRating(currentExpert.getId());
        int totalRatings = ratingRepository.getRatingCount(currentExpert.getId());
        int ratedAnswersCount = ratingRepository.getRatedAnswersCount(currentExpert.getId());
        int answeredQuestionsCount = expertAnswerRepository.getAnsweredQuestionsCount(currentExpert.getId());
        int repliesCount = expertAnswerRepository.getRepliesCount(currentExpert.getId());

        // Show progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Generating Certificate");
        progressAlert.setHeaderText("Please wait...");
        progressAlert.setContentText("AI is creating your personalized certificate of excellence! ✨");
        progressAlert.show();

        // Generate certificate in background thread
        new Thread(() -> {
            try {
                CertificateGenerator generator = new CertificateGenerator();

                // Run certificate generation on FX thread to avoid threading issues
                final File[] certificateFileHolder = new File[1];
                final Exception[] exceptionHolder = new Exception[1];

                Platform.runLater(() -> {
                    try {
                        certificateFileHolder[0] = generator.generateCertificate(
                                currentExpert,
                                avgRating != null ? avgRating : 0.0,
                                totalRatings,
                                answeredQuestionsCount,
                                ratedAnswersCount,
                                repliesCount);
                    } catch (Exception e) {
                        exceptionHolder[0] = e;
                    }
                });

                // Wait for FX thread to complete
                Thread.sleep(100);
                int maxWait = 100; // 10 seconds max
                while (certificateFileHolder[0] == null && exceptionHolder[0] == null && maxWait > 0) {
                    Thread.sleep(100);
                    maxWait--;
                }

                if (exceptionHolder[0] != null) {
                    throw exceptionHolder[0];
                }

                File certificateFile = certificateFileHolder[0];

                // Check if certificate generation failed
                if (certificateFile == null) {
                    logger.error("Certificate generation returned null");
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showErrorAlert("Certificate Generation Failed",
                                "Failed to generate the certificate. Please try again.");
                    });
                    return;
                }

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    progressAlert.close();

                    // Show success dialog
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Certificate Generated! 🎉");
                    successAlert.setHeaderText("Congratulations on achieving 4.8+ Average Rating!");
                    successAlert.setContentText(
                            "Your certificate of excellence has been saved to:\n\n" +
                                    certificateFile.getAbsolutePath() + "\n\n" +
                                    "Would you like to open it now?");

                    ButtonType openButton = new ButtonType("Open Certificate", ButtonBar.ButtonData.OK_DONE);
                    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                    successAlert.getButtonTypes().setAll(openButton, closeButton);

                    successAlert.showAndWait().ifPresent(response -> {
                        if (response == openButton) {
                            try {
                                // Open the certificate file with default image viewer
                                Desktop.getDesktop().open(certificateFile);
                            } catch (Exception e) {
                                logger.error("Failed to open certificate file", e);
                                showErrorAlert("Error", "Failed to open certificate: " + e.getMessage());
                            }
                        }
                    });
                });

            } catch (Exception e) {
                logger.error("Failed to generate certificate", e);
                e.printStackTrace(); // Print stack trace for debugging
                Platform.runLater(() -> {
                    progressAlert.close();
                    showErrorAlert("Error", "Failed to generate certificate: " + e.getMessage());
                });
            }
        }).start();
    }
}
