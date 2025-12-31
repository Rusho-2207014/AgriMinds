package com.agriminds.controller;

import com.agriminds.model.Farmer;
import com.agriminds.model.FarmerCrop;
import com.agriminds.model.PriceNegotiation;
import com.agriminds.repository.FarmerCropRepository;
import com.agriminds.repository.PriceNegotiationRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class OffersController {

    private static final Logger logger = LoggerFactory.getLogger(OffersController.class);

    @FXML
    private VBox offersContainer;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private Button refreshButton;

    private Farmer currentUser;
    private PriceNegotiationRepository negotiationRepository;
    private FarmerCropRepository farmerCropRepository;

    public OffersController() {
        this.negotiationRepository = new PriceNegotiationRepository();
        this.farmerCropRepository = new FarmerCropRepository();
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        loadOffers();
    }

    @FXML
    public void initialize() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All", "Pending", "Accepted", "Rejected");
            statusFilter.setValue("All");
            statusFilter.setOnAction(e -> loadOffers());
        }
    }

    @FXML
    private void handleRefresh() {
        loadOffers();
    }

    private void loadOffers() {
        if (offersContainer == null || currentUser == null) {
            return;
        }

        offersContainer.getChildren().clear();

        List<PriceNegotiation> allOffers = negotiationRepository.findByFarmerId(currentUser.getId());
        logger.info("Retrieved {} total offers for farmer ID: {}", allOffers.size(), currentUser.getId());

        String filter = statusFilter != null ? statusFilter.getValue() : "All";
        List<PriceNegotiation> filteredOffers = allOffers;

        if (!"All".equals(filter)) {
            filteredOffers = allOffers.stream()
                    .filter(o -> filter.equals(o.getStatus()))
                    .toList();
        }

        if (filteredOffers.isEmpty()) {
            Label noOffers = new Label("No buyer offers found");
            noOffers.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            offersContainer.getChildren().add(noOffers);
            return;
        }

        for (PriceNegotiation offer : filteredOffers) {
            VBox card = createOfferCard(offer);
            offersContainer.getChildren().add(card);
        }
    }

    private VBox createOfferCard(PriceNegotiation offer) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(1100);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Get crop details
        FarmerCrop crop = farmerCropRepository.findById(offer.getFarmerCropId()).orElse(null);
        String cropName = crop != null ? crop.getCropName() : "Unknown Crop";

        Label title = new Label("🌾 " + cropName);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(offer.getStatus());
        String badgeColor = switch (offer.getStatus()) {
            case "Accepted" -> "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32;";
            case "Rejected" -> "-fx-background-color: #FFCDD2; -fx-text-fill: #C62828;";
            default -> "-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17;";
        };
        statusBadge.setStyle(badgeColor + " -fx-padding: 6 12; -fx-background-radius: 12; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label(offer.getNegotiationDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        header.getChildren().addAll(title, spacer, statusBadge, dateLabel);

        // Details
        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(10);
        details.setPadding(new Insets(10, 0, 10, 0));

        addDetailRow(details, 0, "Buyer:", offer.getBuyerName());
        addDetailRow(details, 1, "Your Asking Price:", "₹" + offer.getFarmerAskingPrice() + " per kg");
        addDetailRow(details, 2, "Buyer's Offer:", "₹" + offer.getOfferedPrice() + " per kg");

        if (crop != null && crop.getQuantity() > 0) {
            BigDecimal quantity = BigDecimal.valueOf(crop.getQuantity());
            BigDecimal total = offer.getOfferedPrice().multiply(quantity);
            addDetailRow(details, 3, "Total Amount:", String.format("₹%.2f (%.2f kg)", total, crop.getQuantity()));
        }

        card.getChildren().addAll(header, new Separator(), details);

        // Action buttons for pending offers
        if ("Pending".equals(offer.getStatus())) {
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_LEFT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            Button acceptBtn = new Button("✓ Accept Offer");
            acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");
            acceptBtn.setOnAction(e -> acceptOffer(offer));

            Button rejectBtn = new Button("✗ Reject Offer");
            rejectBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");
            rejectBtn.setOnAction(e -> rejectOffer(offer));

            buttonBox.getChildren().addAll(acceptBtn, rejectBtn);
            card.getChildren().add(buttonBox);
        }

        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void acceptOffer(PriceNegotiation offer) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Accept Offer");
        confirmation.setHeaderText("Accept this buyer's offer?");
        confirmation.setContentText(String.format(
                "Buyer: %s\nOffer: ₹%s per kg\n\nDo you want to accept this offer?",
                offer.getBuyerName(), offer.getOfferedPrice()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean updated = negotiationRepository.updateStatus(offer.getId(), "Accepted");

            if (updated) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Offer Accepted");
                successAlert.setContentText("The buyer's offer has been accepted!");
                successAlert.showAndWait();

                loadOffers();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to accept offer");
                errorAlert.setContentText("An error occurred. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }

    private void rejectOffer(PriceNegotiation offer) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reject Offer");
        confirmation.setHeaderText("Reject this buyer's offer?");
        confirmation.setContentText(String.format(
                "Buyer: %s\nOffer: ₹%s per kg\n\nDo you want to reject this offer?",
                offer.getBuyerName(), offer.getOfferedPrice()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean updated = negotiationRepository.updateStatus(offer.getId(), "Rejected");

            if (updated) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Offer Rejected");
                successAlert.setContentText("The buyer's offer has been rejected.");
                successAlert.showAndWait();

                loadOffers();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to reject offer");
                errorAlert.setContentText("An error occurred. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }
}
