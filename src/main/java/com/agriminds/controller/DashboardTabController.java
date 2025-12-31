package com.agriminds.controller;

import com.agriminds.model.Farmer;
import com.agriminds.model.Question;
import com.agriminds.model.ExpertAnswer;
import com.agriminds.repository.QuestionRepository;
import com.agriminds.repository.ExpertAnswerRepository;
import com.agriminds.repository.RatingRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardTabController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardTabController.class);

    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox expertAnswersContainer;

    private Farmer currentUser;
    private QuestionRepository questionRepository;
    private ExpertAnswerRepository expertAnswerRepository;
    private RatingRepository ratingRepository;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    // History storage for cleared items
    private List<Question> clearedAnswers = new java.util.ArrayList<>();
    private boolean isShowingHistory = false;
    private LocalDateTime lastClearTime = null; // Track when dashboard was cleared

    public DashboardTabController() {
        this.questionRepository = new QuestionRepository();
        this.expertAnswerRepository = new ExpertAnswerRepository();
        this.ratingRepository = new RatingRepository();
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        updateWelcomeMessage();
        loadExpertAnswers();
    }

    @FXML
    public void initialize() {
        // Will be populated when setCurrentUser is called
    }

    private void updateWelcomeMessage() {
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");
        }
    }

    @FXML
    private void handleClearDashboard() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Dashboard");
        alert.setHeaderText("Clear all items from dashboard?");
        alert.setContentText("Items will be moved to history. Click 'Previous History' to view them again.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Save current items to history before clearing
                if (currentUser != null && !isShowingHistory) {
                    // Only save new history if we're not already showing history
                    List<Question> allQuestions = questionRepository.findByFarmerId(currentUser.getId());
                    clearedAnswers = allQuestions.stream()
                            .filter(q -> "Answered".equalsIgnoreCase(q.getStatus()))
                            .toList();
                }

                // Clear the display and reset flag
                isShowingHistory = false;
                lastClearTime = LocalDateTime.now(); // Record when cleared

                expertAnswersContainer.getChildren().clear();
                Label answersPlaceholder = new Label(
                        "Dashboard cleared. Click 'Previous History' to view cleared items.");
                answersPlaceholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px; -fx-font-style: italic;");
                answersPlaceholder.setWrapText(true);
                expertAnswersContainer.getChildren().add(answersPlaceholder);
            }
        });
    }

    @FXML
    private void handleShowHistory() {
        if (clearedAnswers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No History");
            alert.setHeaderText("No previous history available");
            alert.setContentText("Clear the dashboard first to create history.");
            alert.showAndWait();
            return;
        }

        isShowingHistory = true;

        // Display cleared answers
        expertAnswersContainer.getChildren().clear();
        if (clearedAnswers.isEmpty()) {
            Label placeholder = new Label("No expert answers in history.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            expertAnswersContainer.getChildren().add(placeholder);
        } else {
            Label historyLabel = new Label("📜 Showing Previous History");
            historyLabel.setStyle(
                    "-fx-text-fill: #2196F3; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
            expertAnswersContainer.getChildren().add(historyLabel);

            for (Question question : clearedAnswers) {
                VBox answerCard = createExpertAnswerCard(question);
                expertAnswersContainer.getChildren().add(answerCard);
            }
        }
    }

    private void loadExpertAnswers() {
        if (expertAnswersContainer == null || currentUser == null)
            return;

        expertAnswersContainer.getChildren().clear();

        // Get only ANSWERED questions
        List<Question> allQuestions = questionRepository.findByFarmerId(currentUser.getId());
        List<Question> answeredQuestions = allQuestions.stream()
                .filter(q -> "Answered".equalsIgnoreCase(q.getStatus()))
                .filter(q -> {
                    // If dashboard was cleared, only show answers received AFTER clear time
                    if (lastClearTime != null && q.getAnsweredDate() != null) {
                        return q.getAnsweredDate().isAfter(lastClearTime);
                    }
                    // If never cleared, show all answered questions
                    return lastClearTime == null;
                })
                .toList();

        if (answeredQuestions.isEmpty()) {
            Label placeholder = new Label("No expert answers yet. Ask questions in the 'Ask Expert' tab.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            expertAnswersContainer.getChildren().add(placeholder);
        } else {
            for (Question question : answeredQuestions) {
                VBox answerCard = createExpertAnswerCard(question);
                expertAnswersContainer.getChildren().add(answerCard);
            }
        }
    }

    private VBox createExpertAnswerCard(Question question) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

        String category = question.getCategory() != null ? question.getCategory() : "General";
        Label categoryLabel = new Label("💡 " + category);
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        categoryLabel.setStyle("-fx-text-fill: #E65100;");

        Label questionLabel = new Label("Q: " + question.getQuestionText());
        questionLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        questionLabel.setStyle("-fx-text-fill: #333;");
        questionLabel.setWrapText(true);

        // Get latest expert answer from expert_answers table
        List<ExpertAnswer> expertAnswers = expertAnswerRepository.getAnswersByQuestionId(question.getId());

        String answer = "No answer provided yet.";
        String expertInfo = "";
        java.time.LocalDateTime answeredDate = null;

        if (!expertAnswers.isEmpty()) {
            // Get the latest expert answer
            ExpertAnswer latestAnswer = expertAnswers.get(expertAnswers.size() - 1);
            answer = latestAnswer.getAnswerText();
            answeredDate = latestAnswer.getAnsweredDate();

            // Get rating info for this expert
            Double avgRating = ratingRepository.getAverageRating(latestAnswer.getExpertId());
            int ratingCount = ratingRepository.getRatingCount(latestAnswer.getExpertId());
            String ratingDisplay = RatingRepository.formatRating(avgRating, ratingCount);

            expertInfo = "By: " + latestAnswer.getExpertName() + " " + ratingDisplay;
        }

        Label answerLabel = new Label("A: " + answer);
        answerLabel.setFont(Font.font("Arial", 13));
        answerLabel.setStyle("-fx-text-fill: #555; -fx-background-color: white; -fx-padding: 10; " +
                "-fx-background-radius: 5; -fx-border-color: #FFE0B2; -fx-border-radius: 5;");
        answerLabel.setWrapText(true);

        if (answeredDate != null) {
            Label expertLabel = new Label(expertInfo);
            expertLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-font-style: italic;");

            Label dateLabel = new Label("Answered: " + answeredDate.format(dateFormatter));
            dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-style: italic;");
            card.getChildren().addAll(categoryLabel, questionLabel, answerLabel, expertLabel, dateLabel);
        } else {
            card.getChildren().addAll(categoryLabel, questionLabel, answerLabel);
        }

        return card;
    }
}
