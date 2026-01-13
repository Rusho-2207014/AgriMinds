package com.agriminds.controller;

import com.agriminds.model.Expert;
import com.agriminds.model.Farmer;
import com.agriminds.model.LoginHistory;
import com.agriminds.repository.LoginHistoryRepository;

import com.agriminds.service.ExpertService;
import com.agriminds.service.FarmerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private RadioButton farmerRadio;
    @FXML
    private RadioButton expertRadio;

    @FXML
    private ToggleGroup userTypeGroup;
    @FXML
    private ComboBox<String> emailCombo;
    @FXML
    private ComboBox<String> passwordCombo;
    @FXML
    private Button loginBtn;
    @FXML
    private Hyperlink registerLink;

    private FarmerService farmerService;
    private ExpertService expertService;

    private LoginHistoryRepository loginHistoryRepository;
    private Stage primaryStage;
    private Object currentUser;
    private String currentUserType;
    private Preferences prefs;
    private Map<String, String> emailPasswordMap;

    public LoginController() {
        this.farmerService = new FarmerService();
        this.expertService = new ExpertService();

        this.loginHistoryRepository = new LoginHistoryRepository();
        this.prefs = Preferences.userNodeForPackage(LoginController.class);
        this.emailPasswordMap = new HashMap<>();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        loadSavedCredentials();
    }

    private void loadSavedCredentials() {
        String savedEmails = prefs.get("saved_emails", "");
        String savedPasswords = prefs.get("saved_passwords", "");

        if (!savedEmails.isEmpty()) {
            String[] emails = savedEmails.split("\\|\\|\\|");
            String[] passwords = savedPasswords.split("\\|\\|\\|");

            for (int i = 0; i < emails.length && i < passwords.length; i++) {
                emailPasswordMap.put(emails[i], passwords[i]);
            }

            emailCombo.getItems().addAll(Arrays.asList(emails));

            emailCombo.setOnAction(e -> {
                String selectedEmail = emailCombo.getValue();
                if (selectedEmail != null && emailPasswordMap.containsKey(selectedEmail)) {
                    passwordCombo.getItems().clear();
                    passwordCombo.getItems().add(emailPasswordMap.get(selectedEmail));
                    passwordCombo.setValue(emailPasswordMap.get(selectedEmail));
                }
            });
        }
    }

    private void saveCredentials(String email, String password) {
        if (!emailPasswordMap.containsKey(email)) {
            emailPasswordMap.put(email, password);

            String emails = String.join("|||", emailPasswordMap.keySet());
            String passwords = String.join("|||", emailPasswordMap.values());

            prefs.put("saved_emails", emails);
            prefs.put("saved_passwords", passwords);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailCombo.getValue();
        String password = passwordCombo.getValue();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showError("Validation Error", "Please enter email and password");
            return;
        }

        currentUserType = farmerRadio.isSelected() ? "farmer" : "expert";

        boolean authenticated = false;
        Long userId = null;

        if ("farmer".equals(currentUserType)) {
            Optional<Farmer> farmerOpt = farmerService.authenticate(email, password);
            if (farmerOpt.isPresent()) {
                currentUser = farmerOpt.get();
                userId = farmerOpt.get().getId();
                authenticated = true;
            }
        } else if ("expert".equals(currentUserType)) {
            Optional<Expert> expertOpt = expertService.authenticate(email, password);
            if (expertOpt.isPresent()) {
                currentUser = expertOpt.get();
                userId = expertOpt.get().getId();
                authenticated = true;
            }

        }

        if (authenticated && userId != null) {
            saveCredentials(email, password);

            Optional<LoginHistory> lastLogin = loginHistoryRepository.getLastSuccessfulLogin(
                    userId, currentUserType);

            LoginHistory currentLogin = new LoginHistory(userId, currentUserType, email);
            currentLogin.setDeviceInfo(System.getProperty("os.name"));
            loginHistoryRepository.save(currentLogin);

            logger.info("User logged in successfully: {}", email);

            if (lastLogin.isPresent()) {
                showLastLoginInfo(lastLogin.get());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Welcome!");
                alert.setHeaderText("First Time Login");
                alert.setContentText("Welcome to AgriMinds! This is your first login.");
                alert.showAndWait();
            }

            loadDashboardForUserType();
        } else {
            LoginHistory failedLogin = new LoginHistory();
            failedLogin.setUserId(0L);
            failedLogin.setUserType(currentUserType);
            failedLogin.setEmail(email);
            failedLogin.setLoginStatus("failed");
            failedLogin.setFailureReason("Invalid credentials");
            failedLogin.setDeviceInfo(System.getProperty("os.name"));
            loginHistoryRepository.save(failedLogin);

            showError("Login Failed", "Invalid email or password");
        }
    }

    @FXML
    private void handleRegister() {
        String userType = farmerRadio.isSelected() ? "farmer" : "expert";
        showRegisterDialog(userType);
    }

    private void loadDashboardForUserType() {
        try {
            String fxmlPath;

            if ("farmer".equals(currentUserType)) {
                fxmlPath = "/fxml/dashboard.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);
                controller.setCurrentUser((Farmer) currentUser);
                controller.initialize();
                primaryStage.setScene(new Scene(root, 1200, 800));

            } else if ("expert".equals(currentUserType)) {
                fxmlPath = "/fxml/expert-dashboard.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                ExpertController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);
                controller.setCurrentExpert((Expert) currentUser);
                primaryStage.setScene(new Scene(root, 1200, 800));

            }

        } catch (Exception e) {
            logger.error("Failed to load dashboard for user type: {}", currentUserType, e);
            showError("Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showRegisterDialog(String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setUserType(userType);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register as " + userType);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception e) {
            logger.error("Failed to load register dialog", e);
            showError("Error", "Failed to load registration form: " + e.getMessage());
        }
    }

    private void showLastLoginInfo(LoginHistory lastLogin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String lastLoginTime = lastLogin.getLoginTime().format(formatter);
        String device = lastLogin.getDeviceInfo() != null ? lastLogin.getDeviceInfo() : "Unknown device";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome Back!");
        alert.setHeaderText("Last Login Information");
        alert.setContentText(String.format(
                "Your last login was on:\n%s\n\nDevice: %s",
                lastLoginTime, device));
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
