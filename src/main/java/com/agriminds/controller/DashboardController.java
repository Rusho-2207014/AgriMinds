package com.agriminds.controller;

import com.agriminds.model.Farmer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab dashboardTab;
    @FXML
    private Tab myCropsTab;
    @FXML
    private Tab askExpertTab;
    @FXML
    private Tab marketTab;
    @FXML
    private Tab offersTab;
    @FXML
    private Tab weatherTab;
    @FXML
    private Tab messagesTab;

    private Stage primaryStage;
    private Farmer currentUser;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
    }

    public void initialize() {
        try {
            loadTabContent(dashboardTab, "/fxml/tabs/dashboard-tab.fxml");
            loadTabContent(myCropsTab, "/fxml/tabs/crops-tab.fxml");
            loadTabContent(askExpertTab, "/fxml/tabs/questions-tab.fxml");
            loadTabContent(marketTab, "/fxml/tabs/market-tab.fxml");
            loadTabContent(offersTab, "/fxml/tabs/offers-tab.fxml");
            loadTabContent(weatherTab, "/fxml/tabs/weather-tab.fxml");
            loadTabContent(messagesTab, "/fxml/tabs/messages-tab.fxml");

        } catch (Exception e) {
            logger.error("Failed to load tab contents", e);
        }
    }

    private void loadTabContent(Tab tab, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            tab.setContent(content);

            Object controller = loader.getController();
            if (controller instanceof CropsController) {
                ((CropsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof QuestionsController) {
                QuestionsController qController = (QuestionsController) controller;
                qController.setCurrentUser(currentUser);
                qController.setMainTabPane(mainTabPane);
                qController.setMessagesTab(messagesTab);
            } else if (controller instanceof WeatherController) {
                ((WeatherController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof OffersController) {
                ((OffersController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof MarketController) {
                ((MarketController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof DashboardTabController) {
                ((DashboardTabController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof MessagesController) {
                MessagesController mController = (MessagesController) controller;
                mController.setCurrentUser(currentUser);
                // Store controller reference in tab userData for later access
                content.setUserData(mController);
            }

        } catch (Exception e) {
            logger.error("Failed to load tab: " + fxmlPath, e);
            tab.setContent(new Label("Error loading content"));
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);

        } catch (Exception e) {
            logger.error("Failed to logout", e);
        }
    }

    @FXML
    private void handleExit() {
        primaryStage.close();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About AgriMinds");
        alert.setHeaderText("AgriMinds v1.0.0");
        alert.setContentText("Smart Farming Platform for Bangladeshi Farmers\n\n" +
                "Developed by: Redoanul Karim\n" +
                "Email: rushorkr2244@gmail.com");
        alert.showAndWait();
    }
}
