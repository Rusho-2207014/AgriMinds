package com.agriminds.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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
        String url = String.format("%s/current.json?key=%s&q=%s,Bangladesh&aqi=no",
                BASE_URL, API_KEY, location);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                logger.info("Weather data fetched for: {}", location);
                return gson.fromJson(jsonResponse, JsonObject.class);
            }
        } catch (Exception e) {
            logger.error("Error fetching weather data", e);
            return createErrorResponse("Failed to fetch weather data");
        }
    }

    public JsonObject getForecast(String location, int days) {
        if (days > 10)
            days = 10;
        String url = String.format("%s/forecast.json?key=%s&q=%s,Bangladesh&days=%d&aqi=no",
                BASE_URL, API_KEY, location, days);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                logger.info("Weather forecast fetched for: {}", location);
                return gson.fromJson(jsonResponse, JsonObject.class);
            }
        } catch (Exception e) {
            logger.error("Error fetching weather forecast", e);
            return createErrorResponse("Failed to fetch weather forecast");
        }
    }

    public boolean hasWeatherAlert(String location) {
        JsonObject forecast = getForecast(location, 3);
        if (forecast.has("error")) {
            return false;
        }
        try {
            JsonObject current = forecast.getAsJsonObject("current");
            double temp = current.get("temp_c").getAsDouble();
            double rainfall = current.get("precip_mm").getAsDouble();
            double windSpeed = current.get("wind_kph").getAsDouble();
            if (temp > 40 || temp < 10)
                return true;
            if (rainfall > 50)
                return true;
            if (windSpeed > 60)
                return true;
        } catch (Exception e) {
            logger.error("Error parsing weather alert data", e);
        }
        return false;
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", true);
        error.addProperty("message", message);
        return error;
    }
}
