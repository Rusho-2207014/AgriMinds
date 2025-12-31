package com.agriminds.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String API_KEY = "your_api_key_here";
    private static final String BASE_URL = "https://api.weatherapi.com/v1";
    private final Gson gson;

    public WeatherService() {
        this.gson = new Gson();
    }

    public JsonObject getCurrentWeather(String location) {
        logger.warn("Weather API integration temporarily disabled");
        return createErrorResponse("Weather service temporarily unavailable");
    }

    public JsonObject getForecast(String location, int days) {
        logger.warn("Weather API integration temporarily disabled");
        return createErrorResponse("Weather service temporarily unavailable");
    }

    public boolean hasWeatherAlert(String location) {
        return false;
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", true);
        error.addProperty("message", message);
        return error;
    }
}
