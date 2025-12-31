package com.agriminds.controller;

import com.agriminds.model.Buyer;
import com.agriminds.model.Expert;
import com.agriminds.model.Farmer;
import com.agriminds.service.BuyerService;
import com.agriminds.service.ExpertService;
import com.agriminds.service.FarmerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> districtCombo;
    @FXML
    private Button registerBtn;
    @FXML
    private Button cancelBtn;

    private String userType;
    private FarmerService farmerService;
    private ExpertService expertService;
    private BuyerService buyerService;

    public RegisterController() {
        this.farmerService = new FarmerService();
        this.expertService = new ExpertService();
        this.buyerService = new BuyerService();
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @FXML
    private void initialize() {
        if (districtCombo != null) {
            districtCombo.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Khulna",
                    "Sylhet", "Barisal", "Rangpur", "Mymensingh");
        }
    }

    @FXML
    private void handleRegister() {
        if (!validateInputs()) {
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Validation Error", "Passwords do not match");
            return;
        }

        try {
            Long userId = null;

            if ("farmer".equals(userType)) {
                Farmer farmer = new Farmer();
                farmer.setFullName(nameField.getText());
                farmer.setEmail(emailField.getText());
                farmer.setPhoneNumber(phoneField.getText());
                farmer.setDistrict(districtCombo.getValue());
                userId = farmerService.registerFarmer(farmer, passwordField.getText());

            } else if ("expert".equals(userType)) {
                Expert expert = new Expert();
                expert.setFullName(nameField.getText());
                expert.setEmail(emailField.getText());
                expert.setPhoneNumber(phoneField.getText());
                expert.setLocation(districtCombo.getValue());
                userId = expertService.register(expert, passwordField.getText());

            } else if ("buyer".equals(userType)) {
                Buyer buyer = new Buyer();
                buyer.setFullName(nameField.getText());
                buyer.setEmail(emailField.getText());
                buyer.setPhoneNumber(phoneField.getText());
                buyer.setDistrict(districtCombo.getValue());
                userId = buyerService.register(buyer, passwordField.getText());
            }

            if (userId != null) {
                showInfo("Success", "Registration successful! You can now login.");
                closeDialog();
            } else {
                showError("Registration Failed", "Failed to register. Email or phone may already exist.");
            }
        } catch (IllegalArgumentException e) {
            showError("Registration Failed", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError("Validation Error", "Please fill all required fields");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            showError("Validation Error", "Password must be at least 8 characters");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) registerBtn.getScene().getWindow();
        stage.close();
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
