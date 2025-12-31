package com.agriminds.controller;

import com.agriminds.model.Farmer;
import com.agriminds.model.FarmerCrop;
import com.agriminds.model.MarketPrice;
import com.agriminds.repository.FarmerCropRepository;
import com.agriminds.repository.MarketPriceRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    @FXML
    private VBox marketPricesContainer;
    @FXML
    private Button addPriceBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private TabPane marketTabPane;
    @FXML
    private Tab generalPricesTab;
    @FXML
    private Tab myCropPricesTab;
    @FXML
    private VBox generalPricesContainer;
    @FXML
    private VBox myCropPricesContainer;

    private Farmer currentUser;
    private MarketPriceRepository marketPriceRepository;
    private FarmerCropRepository farmerCropRepository;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public MarketController() {
        this.marketPriceRepository = new MarketPriceRepository();
        this.farmerCropRepository = new FarmerCropRepository();
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        loadMarketPrices();
        loadMyCropPrices();
    }

    @FXML
    public void initialize() {
        // Initial load will happen when setCurrentUser is called
    }

    @FXML
    private void handleRefresh() {
        loadMarketPrices();
        loadMyCropPrices();
    }

    @FXML
    private void handleAddPrice() {
        showAddPriceDialog();
    }

    private void loadMarketPrices() {
        if (generalPricesContainer == null)
            return;

        generalPricesContainer.getChildren().clear();

        List<MarketPrice> prices = marketPriceRepository.findLatestPrices();

        if (prices.isEmpty()) {
            Label placeholder = new Label("No market prices available yet.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            generalPricesContainer.getChildren().add(placeholder);
        } else {
            for (MarketPrice price : prices) {
                VBox priceCard = createMarketPriceCard(price);
                generalPricesContainer.getChildren().add(priceCard);
            }
        }
    }

    private void loadMyCropPrices() {
        if (myCropPricesContainer == null || currentUser == null)
            return;

        myCropPricesContainer.getChildren().clear();

        List<FarmerCrop> crops = farmerCropRepository.findByFarmerId(currentUser.getId());

        if (crops.isEmpty()) {
            Label placeholder = new Label(
                    "You haven't added any crops with prices yet.\nAdd crops in the 'My Crops' tab.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            myCropPricesContainer.getChildren().add(placeholder);
        } else {
            for (FarmerCrop crop : crops) {
                VBox cropCard = createFarmerCropPriceCard(crop);
                myCropPricesContainer.getChildren().add(cropCard);
            }
        }
    }

    private VBox createMarketPriceCard(MarketPrice price) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #4CAF50; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2;");

        Label cropName = new Label("🌾 " + price.getCropName());
        cropName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        cropName.setStyle("-fx-text-fill: #2E7D32;");

        Label priceLabel = new Label("৳" + String.format("%.2f", price.getRetailPrice()) + " per " + price.getUnit());
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #1976D2;");

        Label marketInfo = new Label("📍 " + (price.getMarketName() != null ? price.getMarketName() : "N/A") +
                ", " + (price.getDistrict() != null ? price.getDistrict() : ""));
        marketInfo.setStyle("-fx-text-fill: #666;");

        Label dateLabel = new Label(
                "📅 " + (price.getPriceDate() != null ? price.getPriceDate().format(dateFormatter) : ""));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        card.getChildren().addAll(cropName, priceLabel, marketInfo, dateLabel);
        return card;
    }

    private VBox createFarmerCropPriceCard(FarmerCrop crop) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #FF9800; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2;");

        Label cropName = new Label("🌾 " + crop.getCropName());
        cropName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        cropName.setStyle("-fx-text-fill: #E65100;");

        Label priceLabel = new Label("৳" + String.format("%.2f", crop.getSellingPrice()) + " per " + crop.getUnit());
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #F57C00;");

        Label qtyLabel = new Label("📦 Quantity: " + String.format("%.2f", crop.getQuantity()) + " " + crop.getUnit());
        qtyLabel.setStyle("-fx-text-fill: #666;");

        String harvestDateStr = crop.getHarvestDate() != null ? crop.getHarvestDate().format(dateFormatter)
                : "Not specified";
        Label dateLabel = new Label("📅 Harvest Date: " + harvestDateStr);
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Label statusLabel = new Label(crop.isAvailable() ? "✅ Available" : "❌ Sold Out");
        statusLabel.setStyle(
                "-fx-text-fill: " + (crop.isAvailable() ? "#2E7D32" : "#C62828") + "; -fx-font-weight: bold;");

        card.getChildren().addAll(cropName, priceLabel, qtyLabel, dateLabel, statusLabel);
        return card;
    }

    private void showAddPriceDialog() {
        if (currentUser == null) {
            showError("Not Logged In", "Please login to add market prices");
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("Add Market Price");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("Add Market Price Information");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label cropLabel = new Label("Crop Name:");
        TextField cropField = new TextField();
        cropField.setPromptText("e.g., Rice, Wheat");

        Label marketLabel = new Label("Market Name:");
        TextField marketField = new TextField();
        marketField.setPromptText("e.g., Karwan Bazar");

        Label districtLabel = new Label("District:");
        TextField districtField = new TextField();
        districtField.setText(currentUser.getDistrict() != null ? currentUser.getDistrict() : "");
        districtField.setPromptText("e.g., Dhaka");

        Label divisionLabel = new Label("Division:");
        TextField divisionField = new TextField();
        divisionField.setText(currentUser.getDivision() != null ? currentUser.getDivision() : "");
        divisionField.setPromptText("e.g., Dhaka");

        Label priceLabel = new Label("Retail Price (৳):");
        TextField priceField = new TextField();
        priceField.setPromptText("Price per kg");

        Label unitLabel = new Label("Unit:");
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("kg", "maund", "quintal", "ton");
        unitCombo.setValue("kg");

        Label sourceLabel = new Label("Source:");
        TextField sourceField = new TextField();
        sourceField.setPromptText("e.g., Local Survey, DAM");
        sourceField.setText("Farmer Survey");

        grid.add(cropLabel, 0, 0);
        grid.add(cropField, 1, 0);
        grid.add(marketLabel, 0, 1);
        grid.add(marketField, 1, 1);
        grid.add(districtLabel, 0, 2);
        grid.add(districtField, 1, 2);
        grid.add(divisionLabel, 0, 3);
        grid.add(divisionField, 1, 3);
        grid.add(priceLabel, 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(unitLabel, 0, 5);
        grid.add(unitCombo, 1, 5);
        grid.add(sourceLabel, 0, 6);
        grid.add(sourceField, 1, 6);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save Price");
        saveBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 8 20;");
        saveBtn.setOnAction(e -> {
            if (cropField.getText().trim().isEmpty() || priceField.getText().isEmpty()) {
                showError("Validation Error", "Please fill crop name and price");
                return;
            }

            try {
                MarketPrice price = new MarketPrice();
                price.setCropName(cropField.getText().trim());
                price.setMarketName(marketField.getText().trim());
                price.setDistrict(districtField.getText().trim());
                price.setDivision(divisionField.getText().trim());
                price.setRetailPrice(Double.parseDouble(priceField.getText()));
                price.setUnit(unitCombo.getValue());
                price.setSource(sourceField.getText().trim());
                price.setPriceDate(LocalDate.now());

                Long priceId = marketPriceRepository.save(price);

                if (priceId != null) {
                    showInfo("Success", "Market price added successfully!");
                    loadMarketPrices();
                    dialog.close();
                } else {
                    showError("Error", "Failed to save market price");
                }

            } catch (NumberFormatException ex) {
                showError("Validation Error", "Price must be a valid number");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #ccc; -fx-padding: 8 20;");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(title, grid, buttonBox);

        dialog.setScene(new javafx.scene.Scene(root, 450, 450));
        dialog.showAndWait();
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
}
