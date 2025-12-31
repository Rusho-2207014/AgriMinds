package com.agriminds.controller;

import com.agriminds.model.Farmer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WeatherController {

    @FXML
    private Label locationLabel;

    private Farmer currentUser;

    public void setCurrentUser(Farmer user) {
        this.currentUser = user;
        updateLocation();
    }

    private void updateLocation() {
        if (currentUser != null && currentUser.getDistrict() != null) {
            locationLabel.setText("📍 Location: " + currentUser.getDistrict() + ", Bangladesh");
        } else {
            locationLabel.setText("📍 Location: Bangladesh");
        }
    }
}
