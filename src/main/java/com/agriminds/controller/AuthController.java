package com.agriminds.controller;
import com.agriminds.model.Farmer;
import com.agriminds.service.FarmerService;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final FarmerService farmerService;
    private Farmer currentUser;
    public AuthController() {
        this.farmerService = new FarmerService();
    }
    public boolean login(String emailOrPhone, String password) {
        try {
            Optional<Farmer> farmerOpt = farmerService.authenticate(emailOrPhone, password);
            if (farmerOpt.isPresent()) {
                currentUser = farmerOpt.get();
                logger.info("User logged in: {}", currentUser.getFullName());
                return true;
            } else {
                showError("Login Failed", "Invalid email/phone or password");
                return false;
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            showError("Error", "An error occurred during login");
            return false;
        }
    }
    public boolean register(Farmer farmer, String password, String confirmPassword) {
        try {
            if (!password.equals(confirmPassword)) {
                showError("Registration Failed", "Passwords do not match");
                return false;
            }
            Long farmerId = farmerService.registerFarmer(farmer, password);
            logger.info("=== DEBUG: Registration returned farmerId: {} ===", farmerId);
            logger.info("=== DEBUG: farmerId is null? {} ===", (farmerId == null));
            if (farmerId != null) {
                showInfo("Success", "Registration successful! Please login.");
                return true;
            } else {
                showError("Registration Failed", "Failed to register. Please try again.");
                return false;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during registration", e);
            showError("Validation Error", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Registration error", e);
            showError("Error", "An error occurred during registration: " + e.getMessage());
            return false;
        }
    }
    public void logout() {
        currentUser = null;
        logger.info("User logged out");
    }
    public Farmer getCurrentUser() {
        return currentUser;
    }
    public boolean isLoggedIn() {
        return currentUser != null;
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
