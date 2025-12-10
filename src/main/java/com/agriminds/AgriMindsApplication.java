package com.agriminds;
import com.agriminds.view.MainFrame;
import com.agriminds.util.DatabaseConnection;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AgriMindsApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(AgriMindsApplication.class);
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting AgriMinds Application...");
            DatabaseConnection.initialize();
            MainFrame mainFrame = new MainFrame();
            mainFrame.show(primaryStage);
            logger.info("AgriMinds Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start AgriMinds Application", e);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start AgriMinds");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check logs for details.");
            alert.showAndWait();
        }
    }
    @Override
    public void stop() {
        try {
            logger.info("Shutting down AgriMinds Application...");
            DatabaseConnection.shutdown();
            logger.info("AgriMinds Application shut down successfully");
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
