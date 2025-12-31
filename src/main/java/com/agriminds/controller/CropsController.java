package com.agriminds.controller;

import com.agriminds.model.Farmer;
import com.agriminds.model.FarmerCrop;
import com.agriminds.repository.FarmerCropRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

import java.util.List;

public class CropsController {

    private static final Logger logger = LoggerFactory.getLogger(CropsController.class);

    @FXML
    private Button addCropBtn;
    @FXML
    private VBox cropsContainer;

    private Farmer currentUser;
    private FarmerCropRepository cropRepository;

    public CropsController() {
        this.cropRepository = new FarmerCropRepository();
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        refreshCropsList();
    }

    @FXML
    private void handleAddCrop() {
        showAddCropDialog();
    }

    private void refreshCropsList() {
        cropsContainer.getChildren().clear();

        if (currentUser == null) {
            Label placeholder = new Label("Please login to view your crops.");
            placeholder.setStyle("-fx-text-fill: #888;");
            cropsContainer.getChildren().add(placeholder);
            return;
        }

        List<FarmerCrop> crops = cropRepository.findByFarmerId(currentUser.getId());

        if (crops.isEmpty()) {
            Label placeholder = new Label(
                    "Your crops will appear here.\nClick 'Add New Crop' to start managing your inventory.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            cropsContainer.getChildren().add(placeholder);
        } else {
            for (FarmerCrop crop : crops) {
                VBox cropCard = createCropCard(crop);
                cropsContainer.getChildren().add(cropCard);
            }
        }
    }

    private VBox createCropCard(FarmerCrop crop) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label cropName = new Label("🌾 " + crop.getCropName());
        cropName.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label details = new Label(String.format("Quantity: %.2f %s | Price: ৳%.2f per %s",
                crop.getQuantity(), crop.getUnit(), crop.getSellingPrice(), crop.getUnit()));
        details.setStyle("-fx-text-fill: #666;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete " + crop.getCropName() + "?");
            confirm.setContentText("Are you sure you want to delete this crop?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cropRepository.delete(crop.getId());
                    refreshCropsList();
                }
            });
        });

        card.getChildren().addAll(cropName, details, deleteBtn);
        return card;
    }

    private void showAddCropDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Crop");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("Add Crop to Your Inventory");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label cropLabel = new Label("Crop Name:");
        TextField cropField = new TextField();
        cropField.setPromptText("Enter crop name");
        cropField.setPrefWidth(250);

        Label qtyLabel = new Label("Quantity:");
        TextField qtyField = new TextField();
        qtyField.setPromptText("e.g., 500");

        Label unitLabel = new Label("Unit:");
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("kg", "ton", "maund", "quintal");
        unitCombo.setValue("kg");
        unitCombo.setPrefWidth(100);

        Label priceLabel = new Label("Selling Price (৳):");
        TextField priceField = new TextField();
        priceField.setPromptText("Price per unit");

        Label dateLabel = new Label("Harvest Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date");

        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Additional details...");
        descArea.setPrefRowCount(3);

        grid.add(cropLabel, 0, 0);
        grid.add(cropField, 1, 0);
        grid.add(qtyLabel, 0, 1);
        grid.add(qtyField, 1, 1);
        grid.add(unitLabel, 0, 2);
        grid.add(unitCombo, 1, 2);
        grid.add(priceLabel, 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(dateLabel, 0, 4);
        grid.add(datePicker, 1, 4);
        grid.add(descLabel, 0, 5);
        grid.add(descArea, 1, 5);

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button saveBtn = new Button("Save Crop");
        saveBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 8 20;");
        saveBtn.setOnAction(e -> {
            if (cropField.getText().trim().isEmpty() || qtyField.getText().isEmpty()
                    || priceField.getText().isEmpty()) {
                showError("Validation Error", "Please fill all required fields");
                return;
            }

            try {
                FarmerCrop crop = new FarmerCrop();
                crop.setFarmerId(currentUser.getId());
                crop.setCropName(cropField.getText());
                crop.setQuantity(Double.parseDouble(qtyField.getText()));
                crop.setUnit(unitCombo.getValue());
                crop.setSellingPrice(Double.parseDouble(priceField.getText()));
                crop.setHarvestDate(datePicker.getValue());
                crop.setDescription(descArea.getText());
                crop.setAvailable(true);

                Long cropId = cropRepository.save(crop);

                if (cropId != null) {
                    showInfo("Success", "Crop added successfully!");
                    refreshCropsList();
                    dialog.close();
                } else {
                    showError("Error", "Failed to save crop");
                }

            } catch (NumberFormatException ex) {
                showError("Validation Error", "Quantity and price must be valid numbers");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #ccc; -fx-padding: 8 20;");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(title, grid, buttonBox);

        dialog.setScene(new javafx.scene.Scene(root, 400, 500));
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
