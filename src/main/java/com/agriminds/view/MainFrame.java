package com.agriminds.view;
import com.agriminds.controller.AuthController;
import com.agriminds.model.FarmerCrop;
import com.agriminds.repository.FarmerCropRepository;
import com.agriminds.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
public class MainFrame {
    private Stage primaryStage;
    private AuthController authController;
    private FarmerCropRepository farmerCropRepository;
    private Scene loginScene;
    private Scene dashboardScene;
    private VBox myCropsTableBox; 
    public MainFrame() {
        this.authController = new AuthController();
        this.farmerCropRepository = new FarmerCropRepository();
    }
    public void show(Stage stage) {
        this.primaryStage = stage;
        loginScene = createLoginScene();
        primaryStage.setTitle(Constants.APP_TITLE);
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(Constants.WINDOW_WIDTH);
        primaryStage.setHeight(Constants.WINDOW_HEIGHT);
        primaryStage.show();
    }
    private Scene createLoginScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("AgriMinds");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: " + Constants.PRIMARY_COLOR + ";");
        Label subtitle = new Label("Smart Farming Platform for Bangladesh");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setStyle("-fx-text-fill: #666;");
        VBox formBox = new VBox(15);
        formBox.setMaxWidth(450);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        Label formTitle = new Label("Login");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Label userTypeLabel = new Label("I am a:");
        userTypeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ToggleGroup userTypeGroup = new ToggleGroup();
        RadioButton farmerRadio = new RadioButton("Farmer ");
        farmerRadio.setToggleGroup(userTypeGroup);
        farmerRadio.setSelected(true);
        farmerRadio.setStyle("-fx-font-size: 13px;");
        RadioButton expertRadio = new RadioButton("Expert ");
        expertRadio.setToggleGroup(userTypeGroup);
        expertRadio.setStyle("-fx-font-size: 13px;");
        RadioButton buyerRadio = new RadioButton("Buyer ");
        buyerRadio.setToggleGroup(userTypeGroup);
        buyerRadio.setStyle("-fx-font-size: 13px;");
        HBox userTypeBox = new HBox(15, farmerRadio, expertRadio, buyerRadio);
        userTypeBox.setAlignment(Pos.CENTER);
        TextField emailField = new TextField();
        emailField.setPromptText("Email or Phone Number");
        emailField.setPrefHeight(40);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(200);
        loginBtn.setPrefHeight(40);
        loginBtn.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; " +
                         "-fx-text-fill: white; -fx-font-size: 14px; " +
                         "-fx-background-radius: 5;");
        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            String userType = farmerRadio.isSelected() ? "farmer" : 
                             expertRadio.isSelected() ? "expert" : "buyer";
            if (authController.login(email, password)) {
                showDashboard();
            }
        });
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setOnAction(e -> {
            String userType = farmerRadio.isSelected() ? "farmer" : 
                             expertRadio.isSelected() ? "expert" : "buyer";
            showRegisterDialog(userType);
        });
        formBox.getChildren().addAll(
            formTitle,
            userTypeLabel,
            userTypeBox,
            emailField,
            passwordField,
            loginBtn,
            registerLink
        );
        root.getChildren().addAll(title, subtitle, formBox);
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }
    private void showDashboard() {
        if (dashboardScene == null) {
            dashboardScene = createDashboardScene();
        }
        primaryStage.setScene(dashboardScene);
    }
    private Scene createDashboardScene() {
        BorderPane root = new BorderPane();
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        TabPane tabPane = new TabPane();
        Tab dashboardTab = new Tab("🏠 Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(createDashboardContent());
        Tab myCropsTab = new Tab("🌾 My Crops");
        myCropsTab.setClosable(false);
        myCropsTab.setContent(createMyCropsContent());
        Tab askExpertTab = new Tab("💬 Ask Expert");
        askExpertTab.setClosable(false);
        askExpertTab.setContent(createAskExpertContent());
        Tab marketTab = new Tab("💰 Market Prices");
        marketTab.setClosable(false);
        marketTab.setContent(createMarketPricesContent());
        Tab offersTab = new Tab("📊 Buyer Offers");
        offersTab.setClosable(false);
        offersTab.setContent(createBuyerOffersContent());
        Tab weatherTab = new Tab("🌤 Weather");
        weatherTab.setClosable(false);
        weatherTab.setContent(createWeatherContent());
        tabPane.getTabs().addAll(dashboardTab, myCropsTab, askExpertTab, marketTab, offersTab, weatherTab);
        root.setCenter(tabPane);
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            authController.logout();
            primaryStage.setScene(loginScene);
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }
    private VBox createDashboardContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label welcome = new Label("Welcome, " + 
            (authController.getCurrentUser() != null ? 
             authController.getCurrentUser().getFullName() : "User") + "!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        HBox cardsBox = new HBox(15);
        cardsBox.getChildren().addAll(
            createDashboardCard("Market Prices", "View today's crop prices"),
            createDashboardCard("Weather Alerts", "Check weather forecasts"),
            createDashboardCard("Soil Health", "Track your soil condition"),
            createDashboardCard("Crop Diseases", "Identify and treat diseases")
        );
        content.getChildren().addAll(welcome, cardsBox);
        return content;
    }
    private VBox createDashboardCard(String title, String description) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-border-color: #ddd; -fx-border-radius: 10;");
        card.setPrefWidth(250);
        card.setPrefHeight(150);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #666;");
        card.getChildren().addAll(titleLabel, descLabel);
        return card;
    }
    private VBox createMyCropsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label title = new Label("My Crop Inventory");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Button addCropBtn = new Button("+ Add New Crop");
        addCropBtn.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; " +
                           "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        addCropBtn.setOnAction(e -> showAddCropDialog());
        myCropsTableBox = new VBox(10);
        myCropsTableBox.setPadding(new Insets(20));
        myCropsTableBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        refreshCropsList();
        content.getChildren().addAll(title, addCropBtn, myCropsTableBox);
        return content;
    }
    private void refreshCropsList() {
        myCropsTableBox.getChildren().clear();
        if (authController.getCurrentUser() == null) {
            Label placeholder = new Label("Please login to view your crops.");
            placeholder.setStyle("-fx-text-fill: #888;");
            myCropsTableBox.getChildren().add(placeholder);
            return;
        }
        List<FarmerCrop> crops = farmerCropRepository.findByFarmerId(authController.getCurrentUser().getId());
        if (crops.isEmpty()) {
            Label placeholder = new Label("Your crops will appear here.\nClick 'Add New Crop' to start managing your inventory.");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            placeholder.setWrapText(true);
            myCropsTableBox.getChildren().add(placeholder);
        } else {
            for (FarmerCrop crop : crops) {
                VBox cropCard = createCropCard(crop);
                myCropsTableBox.getChildren().add(cropCard);
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
        if (crop.getDescription() != null && !crop.getDescription().isEmpty()) {
            Label desc = new Label("Description: " + crop.getDescription());
            desc.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
            desc.setWrapText(true);
            card.getChildren().addAll(cropName, details, desc);
        } else {
            card.getChildren().addAll(cropName, details);
        }
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete " + crop.getCropName() + "?");
            confirm.setContentText("Are you sure you want to delete this crop?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    farmerCropRepository.delete(crop.getId());
                    refreshCropsList();
                }
            });
        });
        card.getChildren().add(deleteBtn);
        return card;
    }
    private VBox createAskExpertContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label title = new Label("Ask Agricultural Expert");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Button askBtn = new Button("+ Ask New Question");
        askBtn.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; " +
                       "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        askBtn.setOnAction(e -> showAskQuestionDialog());
        Label placeholder = new Label("Your questions and expert answers will appear here.\nClick 'Ask New Question' to get help from agricultural experts.");
        placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        placeholder.setWrapText(true);
        VBox questionsBox = new VBox(10);
        questionsBox.setPadding(new Insets(20));
        questionsBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        questionsBox.getChildren().add(placeholder);
        content.getChildren().addAll(title, askBtn, questionsBox);
        return content;
    }
    private VBox createMarketPricesContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label title = new Label("Today's Market Prices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Label info = new Label("Current market prices for major crops (updated daily)");
        info.setStyle("-fx-text-fill: #666;");
        VBox priceBox = new VBox(10);
        priceBox.setPadding(new Insets(20));
        priceBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        Label[] prices = {
            new Label("🌾 Rice: ৳55/kg (Karwan Bazar, Dhaka)"),
            new Label("🥔 Potato: ৳25/kg (Munshiganj Market)"),
            new Label("🍅 Tomato: ৳60/kg (Karwan Bazar, Dhaka)"),
            new Label("🧅 Onion: ৳80/kg (Sher-e-Bangla Nagar)"),
            new Label("🌽 Wheat: ৳45/kg (Badamtoli Bazar, Chittagong)")
        };
        for (Label price : prices) {
            price.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
            priceBox.getChildren().add(price);
        }
        content.getChildren().addAll(title, info, priceBox);
        return content;
    }
    private VBox createBuyerOffersContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label title = new Label("Buyer Price Offers");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Label info = new Label("View and respond to buyer offers for your crops");
        info.setStyle("-fx-text-fill: #666;");
        Label placeholder = new Label("No buyer offers yet.\nWhen buyers make offers for your crops, they will appear here.");
        placeholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        placeholder.setWrapText(true);
        VBox offersBox = new VBox(10);
        offersBox.setPadding(new Insets(20));
        offersBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        offersBox.getChildren().add(placeholder);
        content.getChildren().addAll(title, info, offersBox);
        return content;
    }
    private VBox createWeatherContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label title = new Label("Weather Forecast");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Label location = new Label("📍 Location: " + 
            (authController.getCurrentUser() != null ? 
             authController.getCurrentUser().getDistrict() + ", Bangladesh" : "Bangladesh"));
        location.setStyle("-fx-font-size: 16px;");
        VBox weatherBox = new VBox(15);
        weatherBox.setPadding(new Insets(20));
        weatherBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        Label today = new Label("Today: ☀️ Sunny, 28°C - 35°C");
        Label tomorrow = new Label("Tomorrow: ⛅ Partly Cloudy, 27°C - 33°C");
        Label alert = new Label("⚠️ Weather Alert: Light rain expected in 3 days");
        alert.setStyle("-fx-text-fill: #ff6b00; -fx-font-weight: bold;");
        today.setStyle("-fx-font-size: 14px;");
        tomorrow.setStyle("-fx-font-size: 14px;");
        weatherBox.getChildren().addAll(today, tomorrow, alert);
        content.getChildren().addAll(title, location, weatherBox);
        return content;
    }
    private void showAddCropDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Crop");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
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
        cropField.setPromptText("Enter crop name (e.g., Rice, Wheat, Tomato)");
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
        descArea.setPromptText("Additional details about your crop...");
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
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button saveBtn = new Button("Save Crop");
        saveBtn.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; " +
                        "-fx-text-fill: white; -fx-padding: 8 20;");
        saveBtn.setOnAction(e -> {
            if (cropField.getText() == null || cropField.getText().trim().isEmpty()) {
                showError("Validation Error", "Please enter crop name");
                return;
            }
            if (qtyField.getText().isEmpty()) {
                showError("Validation Error", "Please enter quantity");
                return;
            }
            if (priceField.getText().isEmpty()) {
                showError("Validation Error", "Please enter selling price");
                return;
            }
            try {
                double quantity = Double.parseDouble(qtyField.getText());
                double price = Double.parseDouble(priceField.getText());
                FarmerCrop crop = new FarmerCrop();
                crop.setFarmerId(authController.getCurrentUser().getId());
                crop.setCropName(cropField.getText());
                crop.setQuantity(quantity);
                crop.setUnit(unitCombo.getValue());
                crop.setSellingPrice(price);
                crop.setHarvestDate(datePicker.getValue());
                crop.setDescription(descArea.getText());
                crop.setAvailable(true);
                Long cropId = farmerCropRepository.save(crop);
                if (cropId != null) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("Crop Added Successfully!");
                    success.setContentText(String.format(
                        "Crop: %s\nQuantity: %.2f %s\nPrice: ৳%.2f per %s\n\nYour crop is now available for buyers to view.",
                        cropField.getText(), quantity, unitCombo.getValue(), price, unitCombo.getValue()
                    ));
                    success.showAndWait();
                    refreshCropsList();
                    dialog.close();
                } else {
                    showError("Error", "Failed to save crop. Please try again.");
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
        Scene scene = new Scene(root, 400, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAskQuestionDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Ask Question to Expert");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("Ask Agricultural Expert");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "Disease (রোগ)", 
            "Pest (পোকামাকড়)", 
            "Soil (মাটি)", 
            "Weather (আবহাওয়া)", 
            "Fertilizer (সার)",
            "General (সাধারণ)"
        );
        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(300);
        Label questionLabel = new Label("Your Question:");
        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Describe your problem or question in detail...\nউদাহরণ: আমার ধান গাছের পাতায় বাদামী দাগ দেখা যাচ্ছে। কি করব?");
        questionArea.setPrefRowCount(6);
        questionArea.setWrapText(true);
        grid.add(categoryLabel, 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(questionLabel, 0, 1);
        grid.add(questionArea, 1, 1);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button submitBtn = new Button("Submit Question");
        submitBtn.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; " +
                          "-fx-text-fill: white; -fx-padding: 8 20;");
        submitBtn.setOnAction(e -> {
            if (categoryCombo.getValue() == null || categoryCombo.getValue().isEmpty()) {
                showError("Validation Error", "Please select a category");
                return;
            }
            if (questionArea.getText().isEmpty() || questionArea.getText().length() < 10) {
                showError("Validation Error", "Please write your question (at least 10 characters)");
                return;
            }
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText("Question Submitted!");
            success.setContentText(
                "Your question has been submitted to agricultural experts.\n\n" +
                "Category: " + categoryCombo.getValue() + "\n\n" +
                "You will receive an answer from our experts soon.\n" +
                "Check the 'Ask Expert' tab to see responses."
            );
            success.showAndWait();
            dialog.close();
        });
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #ccc; -fx-padding: 8 20;");
        cancelBtn.setOnAction(e -> dialog.close());
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        root.getChildren().addAll(title, grid, buttonBox);
        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private void showRegisterDialog(String userType) {
        Stage registerStage = new Stage();
        String userTypeTitle = userType.equals("farmer") ? "Farmer (কৃষক)" : 
                              userType.equals("expert") ? "Expert (বিশেষজ্ঞ)" : "Buyer (ক্রেতা)";
        registerStage.setTitle("Register as " + userTypeTitle);
        registerStage.initOwner(primaryStage);
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("Create Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: " + Constants.PRIMARY_COLOR + ";");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number (01XXXXXXXXX)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 8 characters)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        ComboBox<String> divisionBox = new ComboBox<>();
        divisionBox.getItems().addAll(Constants.DIVISIONS);
        divisionBox.setPromptText("Select Division");
        TextField districtField = new TextField();
        districtField.setPromptText("District");
        TextField farmSizeField = new TextField();
        farmSizeField.setPromptText("Farm Size (acres)");
        int row = 0;
        grid.add(new Label("Full Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("Password:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("Confirm Password:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);
        grid.add(new Label("Division:"), 0, row);
        grid.add(divisionBox, 1, row++);
        grid.add(new Label("District:"), 0, row);
        grid.add(districtField, 1, row++);
        grid.add(new Label("Farm Size:"), 0, row);
        grid.add(farmSizeField, 1, row++);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(120);
        registerButton.setStyle("-fx-background-color: " + Constants.PRIMARY_COLOR + "; -fx-text-fill: white;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(120);
        registerButton.setOnAction(e -> {
            try {
                com.agriminds.model.Farmer farmer = new com.agriminds.model.Farmer();
                farmer.setFullName(nameField.getText());
                farmer.setEmail(emailField.getText());
                farmer.setPhoneNumber(phoneField.getText());
                farmer.setDivision(divisionBox.getValue());
                farmer.setDistrict(districtField.getText());
                if (!farmSizeField.getText().isEmpty()) {
                    farmer.setFarmSize(Double.parseDouble(farmSizeField.getText()));
                }
                if (authController.register(farmer, passwordField.getText(), confirmPasswordField.getText())) {
                    registerStage.close();
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setContentText("Please enter a valid farm size number.");
                alert.showAndWait();
            }
        });
        cancelButton.setOnAction(e -> registerStage.close());
        buttonBox.getChildren().addAll(registerButton, cancelButton);
        root.getChildren().addAll(title, grid, buttonBox);
        Scene scene = new Scene(root, 500, 600);
        registerStage.setScene(scene);
        registerStage.show();
    }
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About AgriMinds");
        alert.setHeaderText(Constants.APP_NAME + " v" + Constants.APP_VERSION);
        alert.setContentText("Smart Farming Platform for Bangladeshi Farmers\n\n" +
                           "Features:\n" +
                           "- Real-time market prices\n" +
                           "- Weather alerts\n" +
                           "- Crop disease identification\n" +
                           "- Soil health management\n" +
                           "- Farmer-Buyer marketplace\n\n" +
                           "© 2025 AgriMinds Development Team");
        alert.showAndWait();
    }
}
