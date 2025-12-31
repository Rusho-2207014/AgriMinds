package com.agriminds.controller;

import com.agriminds.model.Buyer;
import com.agriminds.model.FarmerCrop;
import com.agriminds.model.PriceNegotiation;
import com.agriminds.repository.FarmerCropRepository;
import com.agriminds.repository.PriceNegotiationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuyerController {

    private static final Logger logger = LoggerFactory.getLogger(BuyerController.class);

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private ComboBox<String> cropFilterCombo;
    @FXML
    private VBox cropsContainer;
    @FXML
    private VBox negotiationsContainer;
    @FXML
    private VBox purchaseHistoryContainer;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab negotiationsTab;
    @FXML
    private Tab purchaseHistoryTab;

    private Stage primaryStage;
    private Buyer currentBuyer;
    private FarmerCropRepository farmerCropRepository;
    private PriceNegotiationRepository negotiationRepository;
    private List<PriceNegotiation> clearedPurchaseHistory = new ArrayList<>();

    public BuyerController() {
        this.farmerCropRepository = new FarmerCropRepository();
        this.negotiationRepository = new PriceNegotiationRepository();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setCurrentBuyer(Buyer buyer) {
        this.currentBuyer = buyer;
        // Don't call initializeData here - wait for FXML initialize
    }

    @FXML
    public void initialize() {
        // Add tab selection listeners to auto-refresh data
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == negotiationsTab) {
                    loadNegotiations();
                } else if (newTab == purchaseHistoryTab) {
                    loadPurchaseHistory();
                }
            });
        }

        // Load data after FXML components are ready
        initializeData();
    }

    private void initializeData() {
        if (currentBuyer != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentBuyer.getFullName());
            loadCropFilter(); // Load crop filter dynamically from database
            refreshCrops();
            loadNegotiations();
            loadPurchaseHistory();
        }
    }

    private void loadCropFilter() {
        if (cropFilterCombo != null) {
            cropFilterCombo.getItems().clear();
            cropFilterCombo.getItems().add("All Crops");

            // Get all unique crop names from database
            List<FarmerCrop> allCrops = farmerCropRepository.getAllAvailableCrops();
            List<String> uniqueCropNames = allCrops.stream()
                    .map(FarmerCrop::getCropName)
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .sorted()
                    .toList();

            cropFilterCombo.getItems().addAll(uniqueCropNames);
            cropFilterCombo.setValue("All Crops");
            cropFilterCombo.setOnAction(e -> refreshCrops());
        }
    }

    @FXML
    private void refreshCrops() {
        if (cropsContainer == null) {
            return;
        }

        // Reload the crop filter to include any newly added crops
        loadCropFilter();

        cropsContainer.getChildren().clear();

        List<FarmerCrop> allCrops = farmerCropRepository.getAllAvailableCrops();

        String filter = cropFilterCombo.getValue();
        List<FarmerCrop> filteredCrops = allCrops;

        if (!"All Crops".equals(filter)) {
            filteredCrops = allCrops.stream()
                    .filter(c -> c.getCropName() != null && c.getCropName().equalsIgnoreCase(filter))
                    .toList();
        }

        statsLabel.setText("Available Crops: " + filteredCrops.size());

        if (filteredCrops.isEmpty()) {
            Label noCrops = new Label("No crops available");
            noCrops.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            cropsContainer.getChildren().add(noCrops);
            return;
        }

        for (FarmerCrop crop : filteredCrops) {
            VBox cropCard = createCropCard(crop);
            cropsContainer.getChildren().add(cropCard);
        }
    }

    @FXML
    private void handleRefreshNegotiations() {
        logger.info("Manual refresh of negotiations triggered");
        loadNegotiations();
    }

    @FXML
    private void handleRefreshPurchaseHistory() {
        logger.info("Manual refresh of purchase history triggered");
        loadPurchaseHistory();
    }

    @FXML
    private void handleClearPurchaseHistory() {
        if (purchaseHistoryContainer == null || currentBuyer == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear Purchase History");
        confirmation.setHeaderText("Clear all purchase history?");
        confirmation.setContentText("This will clear the display but your purchase records remain in the database.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Save current purchases to cleared history
            List<PriceNegotiation> currentPurchases = negotiationRepository.findByBuyerId(currentBuyer.getId())
                    .stream()
                    .filter(n -> "Accepted".equals(n.getStatus()))
                    .toList();

            clearedPurchaseHistory.addAll(currentPurchases);

            // Clear the display
            purchaseHistoryContainer.getChildren().clear();
            Label clearedLabel = new Label("Purchase history cleared. Click 'Refresh' to reload.");
            clearedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888; -fx-font-style: italic;");
            purchaseHistoryContainer.getChildren().add(clearedLabel);
        }
    }

    private VBox createCropCard(FarmerCrop crop) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label cropName = new Label("🌾 " + crop.getCropName());
        cropName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label availableBadge = new Label("Available");
        availableBadge.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; " +
                "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        header.getChildren().addAll(cropName, spacer, availableBadge);

        GridPane details = new GridPane();
        details.setHgap(20);
        details.setVgap(8);
        details.setPadding(new Insets(5, 0, 5, 0));

        addDetailRow(details, 0, "Quantity:", String.format("%.2f %s", crop.getQuantity(), crop.getUnit()));
        addDetailRow(details, 1, "Farmer's Asking Price:",
                String.format("₹%.2f per %s", crop.getSellingPrice(), crop.getUnit()));

        if (crop.getHarvestDate() != null) {
            addDetailRow(details, 2, "Harvest Date:", crop.getHarvestDate().toString());
        }

        if (crop.getDescription() != null && !crop.getDescription().isEmpty()) {
            Label descLabel = new Label("Description:");
            descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
            Label descValue = new Label(crop.getDescription());
            descValue.setWrapText(true);
            descValue.setStyle("-fx-text-fill: #333;");
            details.add(descLabel, 0, 3);
            details.add(descValue, 1, 3);
        }

        // Check if this buyer already has a negotiation for this crop
        boolean hasNegotiation = currentBuyer != null && negotiationRepository.findByBuyerId(currentBuyer.getId())
                .stream()
                .anyMatch(n -> n.getFarmerCropId().equals(crop.getId()));

        if (hasNegotiation) {
            // Show "Deal Done" label instead of buttons
            HBox doneBox = new HBox();
            doneBox.setAlignment(Pos.CENTER_LEFT);
            doneBox.setPadding(new Insets(10, 0, 0, 0));

            Label dealDoneLabel = new Label("✅ Deal Done");
            dealDoneLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
            doneBox.getChildren().add(dealDoneLabel);

            card.getChildren().addAll(header, details, doneBox);
        } else {
            // Show buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_LEFT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            Button makeOfferBtn = new Button("💰 Make Offer");
            makeOfferBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
            makeOfferBtn.setOnAction(e -> {
                showMakeOfferDialog(crop, card, buttonBox);
            });

            Button buyNowBtn = new Button("✅ Buy at Asking Price");
            buyNowBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
            buyNowBtn.setOnAction(e -> {
                buyAtAskingPrice(crop, card, buttonBox);
            });

            buttonBox.getChildren().addAll(makeOfferBtn, buyNowBtn);
            card.getChildren().addAll(header, details, buttonBox);
        }

        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #333;");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showMakeOfferDialog(FarmerCrop crop, VBox card, HBox buttonBox) {
        Dialog<PriceNegotiation> dialog = new Dialog<>();
        dialog.setTitle("Make Offer");
        dialog.setHeaderText("Make an offer for " + crop.getCropName());

        ButtonType submitButtonType = new ButtonType("Submit Offer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label askingPriceLabel = new Label(String.format("Farmer's Asking Price: ₹%.2f per %s",
                crop.getSellingPrice(), crop.getUnit()));
        askingPriceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in " + crop.getUnit());
        quantityField.setText(String.valueOf(crop.getQuantity()));

        TextField offerPriceField = new TextField();
        offerPriceField.setPromptText("Your offer price per " + crop.getUnit());

        Label totalLabel = new Label("Total: ₹0.00");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        // Calculate total on input change
        Runnable updateTotal = () -> {
            try {
                double qty = Double.parseDouble(quantityField.getText());
                double price = Double.parseDouble(offerPriceField.getText());
                double total = qty * price;
                totalLabel.setText(String.format("Total: ₹%.2f", total));
            } catch (NumberFormatException e) {
                totalLabel.setText("Total: ₹0.00");
            }
        };

        quantityField.textProperty().addListener((obs, old, newVal) -> updateTotal.run());
        offerPriceField.textProperty().addListener((obs, old, newVal) -> updateTotal.run());

        grid.add(askingPriceLabel, 0, 0, 2, 1);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Your Offer Price:"), 0, 2);
        grid.add(offerPriceField, 1, 2);
        grid.add(totalLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    PriceNegotiation negotiation = new PriceNegotiation();
                    negotiation.setFarmerCropId(crop.getId());
                    negotiation.setBuyerId(currentBuyer.getId());
                    negotiation.setBuyerName(currentBuyer.getFullName());
                    negotiation.setOfferedPrice(new BigDecimal(offerPriceField.getText()));
                    negotiation.setFarmerAskingPrice(BigDecimal.valueOf(crop.getSellingPrice()));
                    negotiation.setQuantityKg(new BigDecimal(quantityField.getText()));
                    negotiation.setStatus("Pending");
                    negotiation.setNegotiationDate(LocalDateTime.now());
                    return negotiation;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<PriceNegotiation> result = dialog.showAndWait();
        result.ifPresent(negotiation -> {
            if (negotiation != null) {
                negotiationRepository.save(negotiation);

                // Replace buttons with "Deal Done" label
                card.getChildren().remove(buttonBox);
                HBox doneBox = new HBox();
                doneBox.setAlignment(Pos.CENTER_LEFT);
                doneBox.setPadding(new Insets(10, 0, 0, 0));
                Label dealDoneLabel = new Label("✅ Deal Done");
                dealDoneLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
                doneBox.getChildren().add(dealDoneLabel);
                card.getChildren().add(doneBox);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Your offer has been submitted! The farmer will review it.");
                successAlert.showAndWait();

                loadNegotiations();
            }
        });
    }

    private void buyAtAskingPrice(FarmerCrop crop, VBox card, HBox buttonBox) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Purchase");
        confirmation.setHeaderText("Buy " + crop.getCropName());
        confirmation.setContentText(String.format(
                "Quantity: %.2f %s\nPrice: ₹%.2f per %s\nTotal: ₹%.2f\n\nConfirm purchase?",
                crop.getQuantity(), crop.getUnit(), crop.getSellingPrice(), crop.getUnit(),
                crop.getQuantity() * crop.getSellingPrice()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            PriceNegotiation negotiation = new PriceNegotiation();
            negotiation.setFarmerCropId(crop.getId());
            negotiation.setBuyerId(currentBuyer.getId());
            negotiation.setBuyerName(currentBuyer.getFullName());
            negotiation.setOfferedPrice(BigDecimal.valueOf(crop.getSellingPrice()));
            negotiation.setFarmerAskingPrice(BigDecimal.valueOf(crop.getSellingPrice()));
            negotiation.setQuantityKg(BigDecimal.valueOf(crop.getQuantity()));
            negotiation.setStatus("Accepted");
            negotiation.setNegotiationDate(LocalDateTime.now());
            negotiation.setAcceptedDate(LocalDateTime.now());

            logger.info("Saving purchase with status: {}", negotiation.getStatus());
            Long savedId = negotiationRepository.save(negotiation);
            logger.info("Purchase saved with ID: {}", savedId);

            // Replace buttons with "Deal Done" label
            card.getChildren().remove(buttonBox);
            HBox doneBox = new HBox();
            doneBox.setAlignment(Pos.CENTER_LEFT);
            doneBox.setPadding(new Insets(10, 0, 0, 0));
            Label dealDoneLabel = new Label("✅ Deal Done");
            dealDoneLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
            doneBox.getChildren().add(dealDoneLabel);
            card.getChildren().add(doneBox);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Purchase Completed!");
            successAlert.setContentText("You have successfully purchased the crop!");
            successAlert.showAndWait();

            logger.info("Refreshing UI after purchase...");
            loadNegotiations();
            loadPurchaseHistory();
            logger.info("UI refresh completed");
        }
    }

    private void loadNegotiations() {
        if (negotiationsContainer == null || currentBuyer == null) {
            return;
        }

        negotiationsContainer.getChildren().clear();

        List<PriceNegotiation> negotiations = negotiationRepository.findByBuyerId(currentBuyer.getId())
                .stream()
                .filter(n -> "Pending".equals(n.getStatus()) || "Counter".equals(n.getStatus()))
                .toList();

        if (negotiations.isEmpty()) {
            Label noNegotiations = new Label("No active negotiations");
            noNegotiations.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            negotiationsContainer.getChildren().add(noNegotiations);
            return;
        }

        for (PriceNegotiation negotiation : negotiations) {
            VBox card = createNegotiationCard(negotiation);
            negotiationsContainer.getChildren().add(card);
        }
    }

    private VBox createNegotiationCard(PriceNegotiation negotiation) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Price Negotiation");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(negotiation.getStatus());
        statusBadge.setStyle("-fx-background-color: #FFECB3; -fx-text-fill: #F57C00; " +
                "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label(negotiation.getNegotiationDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        header.getChildren().addAll(title, spacer, statusBadge, dateLabel);

        GridPane details = new GridPane();
        details.setHgap(20);
        details.setVgap(8);
        details.setPadding(new Insets(5, 0, 5, 0));

        addDetailRow(details, 0, "Quantity:", negotiation.getQuantityKg() + " kg");
        addDetailRow(details, 1, "Your Offer:", "₹" + negotiation.getOfferedPrice() + " per kg");
        addDetailRow(details, 2, "Farmer's Price:", "₹" + negotiation.getFarmerAskingPrice() + " per kg");

        double total = negotiation.getQuantityKg().doubleValue() * negotiation.getOfferedPrice().doubleValue();
        addDetailRow(details, 3, "Total Offer:", String.format("₹%.2f", total));

        card.getChildren().addAll(header, details);
        return card;
    }

    private void loadPurchaseHistory() {
        if (purchaseHistoryContainer == null || currentBuyer == null) {
            logger.info("Purchase history container or buyer is null");
            return;
        }

        purchaseHistoryContainer.getChildren().clear();

        List<PriceNegotiation> allNegotiations = negotiationRepository.findByBuyerId(currentBuyer.getId());
        logger.info("Retrieved {} total negotiations for buyer ID: {}", allNegotiations.size(), currentBuyer.getId());

        List<PriceNegotiation> purchases = allNegotiations.stream()
                .filter(n -> {
                    logger.info("Negotiation ID {}: Status = {}", n.getId(), n.getStatus());
                    return "Accepted".equals(n.getStatus());
                })
                .toList();

        logger.info("Filtered to {} accepted purchases", purchases.size());

        if (purchases.isEmpty()) {
            Label noPurchases = new Label("No purchase history");
            noPurchases.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            purchaseHistoryContainer.getChildren().add(noPurchases);
            return;
        }

        for (PriceNegotiation purchase : purchases) {
            VBox card = createPurchaseCard(purchase);
            purchaseHistoryContainer.getChildren().add(card);
        }
    }

    private VBox createPurchaseCard(PriceNegotiation purchase) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1100);

        // Get crop name from farmerCropId
        String cropName = "Unknown Crop";
        if (purchase.getFarmerCropId() != null) {
            Optional<FarmerCrop> cropOpt = farmerCropRepository.findById(purchase.getFarmerCropId());
            if (cropOpt.isPresent()) {
                cropName = cropOpt.get().getCropName();
            }
        }

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("✅ " + cropName + " - Purchase Completed");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label(purchase.getAcceptedDate() != null ? purchase.getAcceptedDate().format(formatter)
                : purchase.getNegotiationDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        header.getChildren().addAll(title, spacer, dateLabel);

        GridPane details = new GridPane();
        details.setHgap(20);
        details.setVgap(8);
        details.setPadding(new Insets(5, 0, 5, 0));

        addDetailRow(details, 0, "Quantity:", purchase.getQuantityKg() + " kg");
        addDetailRow(details, 1, "Price Paid:", "₹" + purchase.getOfferedPrice() + " per kg");

        double total = purchase.getQuantityKg().doubleValue() * purchase.getOfferedPrice().doubleValue();
        addDetailRow(details, 2, "Total Paid:", String.format("₹%.2f", total));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button undoBtn = new Button("↩ Undo Purchase");
        undoBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 8 15; -fx-cursor: hand;");
        undoBtn.setOnAction(e -> undoPurchase(purchase));

        Button changeBtn = new Button("✏ Change Purchase");
        changeBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 8 15; -fx-cursor: hand;");
        changeBtn.setOnAction(e -> changePurchase(purchase));

        buttonBox.getChildren().addAll(undoBtn, changeBtn);

        card.getChildren().addAll(header, details, buttonBox);
        return card;
    }

    private void undoPurchase(PriceNegotiation purchase) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Undo Purchase");
        confirmation.setHeaderText("Cancel this purchase?");
        confirmation.setContentText("This will cancel your purchase and remove it from your history.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            negotiationRepository.updateStatus(purchase.getId(), "Cancelled");

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Purchase Cancelled");
            successAlert.setContentText("Your purchase has been cancelled.");
            successAlert.showAndWait();

            loadNegotiations();
            loadPurchaseHistory();
            refreshCrops();
        }
    }

    private void changePurchase(PriceNegotiation purchase) {
        Dialog<PriceNegotiation> dialog = new Dialog<>();
        dialog.setTitle("Change Purchase");
        dialog.setHeaderText("Modify your purchase details");

        ButtonType submitButtonType = new ButtonType("Update Purchase", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity in kg");
        quantityField.setText(purchase.getQuantityKg().toString());

        TextField offerPriceField = new TextField();
        offerPriceField.setPromptText("Your offer price per kg");
        offerPriceField.setText(purchase.getOfferedPrice().toString());

        Label totalLabel = new Label("Total: ₹0.00");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Runnable updateTotal = () -> {
            try {
                double qty = Double.parseDouble(quantityField.getText());
                double price = Double.parseDouble(offerPriceField.getText());
                double total = qty * price;
                totalLabel.setText(String.format("Total: ₹%.2f", total));
            } catch (NumberFormatException e) {
                totalLabel.setText("Total: ₹0.00");
            }
        };

        quantityField.textProperty().addListener((obs, old, newVal) -> updateTotal.run());
        offerPriceField.textProperty().addListener((obs, old, newVal) -> updateTotal.run());
        updateTotal.run();

        grid.add(new Label("Quantity (kg):"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Price per kg:"), 0, 1);
        grid.add(offerPriceField, 1, 1);
        grid.add(totalLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    purchase.setQuantityKg(new BigDecimal(quantityField.getText()));
                    purchase.setOfferedPrice(new BigDecimal(offerPriceField.getText()));
                    purchase.setNegotiationDate(LocalDateTime.now());
                    return purchase;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<PriceNegotiation> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            if (updated != null) {
                // Cancel old purchase and create new one
                negotiationRepository.updateStatus(purchase.getId(), "Modified");

                PriceNegotiation newNegotiation = new PriceNegotiation();
                newNegotiation.setFarmerCropId(purchase.getFarmerCropId());
                newNegotiation.setBuyerId(purchase.getBuyerId());
                newNegotiation.setBuyerName(purchase.getBuyerName());
                newNegotiation.setOfferedPrice(updated.getOfferedPrice());
                newNegotiation.setFarmerAskingPrice(purchase.getFarmerAskingPrice());
                newNegotiation.setQuantityKg(updated.getQuantityKg());
                newNegotiation.setStatus("Pending");
                newNegotiation.setNegotiationDate(LocalDateTime.now());

                negotiationRepository.save(newNegotiation);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Purchase Modified");
                successAlert.setContentText("Your purchase has been modified and is now pending farmer approval.");
                successAlert.showAndWait();

                loadNegotiations();
                loadPurchaseHistory();
                refreshCrops();
            }
        });
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
        alert.setHeaderText("Buyer Dashboard");
        alert.setContentText("Purchase quality crops directly from farmers.\nVersion 1.0");
        alert.showAndWait();
    }
}
