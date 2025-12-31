package com.agriminds.service;

import com.agriminds.model.Expert;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.embed.swing.SwingFXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to generate achievement certificates for experts with 5-star ratings
 * Uses AI to create personalized certificate messages
 */
public class CertificateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerator.class);
    private final AIService aiService;

    public CertificateGenerator() {
        this.aiService = new AIService();
    }

    /**
     * Generate a certificate for an expert with statistics
     */
    public File generateCertificate(Expert expert, double avgRating, int totalRatings,
            int questionsAnswered, int ratedAnswers, int repliesCount) throws IOException {
        try {
            logger.info("Generating certificate for expert: {}", expert.getFullName());

            // Use AI to generate a personalized message
            String aiMessage = generateAIMessage(expert, avgRating, totalRatings, questionsAnswered, ratedAnswers,
                    repliesCount);
            logger.info("AI message generated: {}", aiMessage.substring(0, Math.min(100, aiMessage.length())));

            // Create certificate layout
            VBox certificate = createCertificateLayout(expert, aiMessage, avgRating,
                    totalRatings, questionsAnswered, ratedAnswers, repliesCount);

            // Convert to image
            WritableImage image = certificate.snapshot(new SnapshotParameters(), null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Save to file
            String fileName = "Certificate_" + expert.getFullName().replace(" ", "_") +
                    "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            File outputFile = new File(System.getProperty("user.home") + "/Downloads/" + fileName);

            ImageIO.write(bufferedImage, "png", outputFile);
            logger.info("Certificate saved to: {}", outputFile.getAbsolutePath());

            return outputFile;
        } catch (Exception e) {
            logger.error("Error generating certificate", e);
            throw new IOException("Failed to generate certificate: " + e.getMessage(), e);
        }
    }

    /**
     * Generate personalized certificate message using AI
     */
    private String generateAIMessage(Expert expert, double avgRating, int totalRatings,
            int questionsAnswered, int ratedAnswers, int repliesCount) {
        try {
            String prompt = String.format(
                    "Generate a short, inspiring certificate message (max 2 sentences, max 100 words) " +
                            "for agricultural expert %s who achieved an excellent average rating of %.2f stars " +
                            "from %d total ratings. They have answered %d questions, received ratings on %d answers, " +
                            "and provided %d corrections/additions/replies to help other experts. " +
                            "Make it professional, warm, and celebratory. Focus on their expertise and positive impact on farming community.",
                    expert.getFullName(), avgRating, totalRatings, questionsAnswered, ratedAnswers, repliesCount);

            String answer = aiService.getAIAnswer("General", prompt);

            // Extract the certificate message from the AI response
            // Look for content between "Certificate Message" markers or quotes
            String extracted = answer;

            // Try to find the actual certificate message in the response
            if (answer.contains("Certificate Message")) {
                int start = answer.indexOf("Certificate Message");
                int quoteStart = answer.indexOf("\"", start);
                int quoteEnd = answer.indexOf("\"", quoteStart + 1);
                if (quoteStart != -1 && quoteEnd != -1 && quoteEnd > quoteStart) {
                    extracted = answer.substring(quoteStart + 1, quoteEnd);
                }
            }

            // Clean up and limit length
            extracted = extracted.trim().replaceAll("\"", "");

            // If still too long, truncate at sentence boundaries
            if (extracted.length() > 250) {
                String[] sentences = extracted.split("\\.");
                StringBuilder result = new StringBuilder();
                int count = 0;
                for (String sentence : sentences) {
                    if (count >= 2)
                        break;
                    if (!sentence.trim().isEmpty()) {
                        result.append(sentence.trim()).append(". ");
                        count++;
                    }
                }
                extracted = result.toString().trim();
            }

            return extracted;
        } catch (Exception e) {
            logger.error("Failed to generate AI message, using default", e);
            return String.format(
                    "Your exceptional expertise and dedication to helping farmers has earned you an outstanding average rating of %.2f stars from %d ratings. "
                            +
                            "Continue making a positive impact in the agricultural community!",
                    avgRating, totalRatings);
        }
    }

    /**
     * Create the visual certificate layout
     */
    private VBox createCertificateLayout(Expert expert, String aiMessage, double avgRating,
            int totalRatings, int questionsAnswered, int ratedAnswers, int repliesCount) {
        VBox certificate = new VBox(20);
        certificate.setPrefSize(1200, 800);
        certificate.setAlignment(Pos.CENTER);
        certificate.setPadding(new Insets(60));

        // Elegant gradient background
        BackgroundFill bgFill = new BackgroundFill(
                new javafx.scene.paint.LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new javafx.scene.paint.Stop(0, Color.web("#f5f7fa")),
                        new javafx.scene.paint.Stop(1, Color.web("#c3cfe2"))),
                CornerRadii.EMPTY,
                Insets.EMPTY);
        certificate.setBackground(new Background(bgFill));

        // Gold border
        certificate.setBorder(new Border(new BorderStroke(
                Color.web("#FFD700"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(8))));

        // Inner border
        VBox innerBox = new VBox(15);
        innerBox.setAlignment(Pos.CENTER);
        innerBox.setPadding(new Insets(40));
        innerBox.setBorder(new Border(new BorderStroke(
                Color.web("#DAA520"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(2))));

        // Title
        Text title = new Text("CERTIFICATE OF EXCELLENCE");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setFill(Color.web("#2C3E50"));

        // Subtitle
        Text subtitle = new Text("★★★★★");
        subtitle.setFont(Font.font(36));
        subtitle.setFill(Color.web("#FFD700"));

        // Recognition text
        Text recognition = new Text("This certifies that");
        recognition.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        recognition.setFill(Color.web("#555"));

        // Expert name
        Text expertName = new Text(expert.getFullName());
        expertName.setFont(Font.font("Brush Script MT", FontWeight.BOLD, 56));
        expertName.setFill(Color.web("#1A5490"));
        expertName.setUnderline(true);

        // Specialization
        String specializationText = expert.getSpecialization() != null
                ? expert.getSpecialization() + " Expert"
                : "Expert";
        Text specialization = new Text(specializationText);
        specialization.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        specialization.setFill(Color.web("#2E7D32"));

        // Achievement text
        Text achievement = new Text("has achieved an Outstanding Average Rating of 4.8★ or Higher");
        achievement.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        achievement.setFill(Color.web("#555"));

        // AI-generated message
        Text aiText = new Text(aiMessage);
        aiText.setFont(Font.font("Georgia", FontPosture.ITALIC, 16));
        aiText.setFill(Color.web("#444"));
        aiText.setWrappingWidth(900);
        aiText.setTextAlignment(TextAlignment.CENTER);

        // Statistics box
        VBox statisticsBox = new VBox(12);
        statisticsBox.setAlignment(Pos.CENTER);
        statisticsBox.setPadding(new Insets(20));
        statisticsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 8;");

        // Average rating
        Text avgRatingText = new Text(String.format("Average Rating: %.2f ⭐", avgRating));
        avgRatingText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        avgRatingText.setFill(Color.web("#FFD700"));

        // Create statistics row
        HBox statsRow = new HBox(40);
        statsRow.setAlignment(Pos.CENTER);

        VBox totalRatingsBox = new VBox(5);
        totalRatingsBox.setAlignment(Pos.CENTER);
        Text totalRatingsLabel = new Text("Total Ratings");
        totalRatingsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        totalRatingsLabel.setFill(Color.web("#666"));
        Text totalRatingsValue = new Text(String.valueOf(totalRatings));
        totalRatingsValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        totalRatingsValue.setFill(Color.web("#2E7D32"));
        totalRatingsBox.getChildren().addAll(totalRatingsLabel, totalRatingsValue);

        VBox answeredBox = new VBox(5);
        answeredBox.setAlignment(Pos.CENTER);
        Text answeredLabel = new Text("Questions Answered");
        answeredLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        answeredLabel.setFill(Color.web("#666"));
        Text answeredValue = new Text(String.valueOf(questionsAnswered));
        answeredValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        answeredValue.setFill(Color.web("#1976D2"));
        answeredBox.getChildren().addAll(answeredLabel, answeredValue);

        VBox ratedAnswersBox = new VBox(5);
        ratedAnswersBox.setAlignment(Pos.CENTER);
        Text ratedAnswersLabel = new Text("Rated Answers");
        ratedAnswersLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        ratedAnswersLabel.setFill(Color.web("#666"));
        Text ratedAnswersValue = new Text(String.valueOf(ratedAnswers));
        ratedAnswersValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        ratedAnswersValue.setFill(Color.web("#7B1FA2"));
        ratedAnswersBox.getChildren().addAll(ratedAnswersLabel, ratedAnswersValue);

        VBox repliesBox = new VBox(5);
        repliesBox.setAlignment(Pos.CENTER);
        Text repliesLabel = new Text("Corrections/Additions/Replies");
        repliesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        repliesLabel.setFill(Color.web("#666"));
        Text repliesValue = new Text(String.valueOf(repliesCount));
        repliesValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        repliesValue.setFill(Color.web("#FF6F00"));
        repliesBox.getChildren().addAll(repliesLabel, repliesValue);

        statsRow.getChildren().addAll(totalRatingsBox, answeredBox, ratedAnswersBox, repliesBox);

        // Issue date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        Text issuedDate = new Text("Issued: " + LocalDateTime.now().format(formatter));
        issuedDate.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        issuedDate.setFill(Color.web("#666"));

        statisticsBox.getChildren().addAll(avgRatingText, statsRow, issuedDate);

        // Signature line
        Text signature = new Text("AgriMinds Platform");
        signature.setFont(Font.font("Edwardian Script ITC", FontWeight.BOLD, 32));
        signature.setFill(Color.web("#1A5490"));

        Text signatureRole = new Text("Agricultural Excellence Recognition");
        signatureRole.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        signatureRole.setFill(Color.web("#888"));

        // Assembly
        innerBox.getChildren().addAll(
                title,
                subtitle,
                recognition,
                expertName,
                specialization,
                achievement,
                new Region() {
                    {
                        setPrefHeight(10);
                    }
                },
                aiText,
                new Region() {
                    {
                        setPrefHeight(15);
                    }
                },
                statisticsBox,
                new Region() {
                    {
                        setPrefHeight(20);
                    }
                },
                signature,
                signatureRole);

        certificate.getChildren().add(innerBox);
        return certificate;
    }
}
