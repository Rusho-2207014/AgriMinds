package com.agriminds.util;

public class Constants {
    public static final String APP_NAME = "AgriMinds";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_TITLE = "AgriMinds - Smart Farming Platform";
    public static final String[] DIVISIONS = {
            "Dhaka", "Chittagong", "Rajshahi", "Khulna",
            "Barisal", "Sylhet", "Rangpur", "Mymensingh"
    };
    public static final String[] FARMING_TYPES = {
            "Organic", "Traditional", "Mixed", "Commercial", "Subsistence"
    };
    public static final String[] CROP_CATEGORIES = {
            "Cereal", "Vegetable", "Fruit", "Cash Crop", "Spice", "Pulse", "Oilseed"
    };
    public static final String[] SEASONS = {
            "Rabi", "Kharif", "Year-round"
    };
    public static final String[] SOIL_TYPES = {
            "Loamy", "Clay", "Sandy", "Alluvial", "Red", "Black"
    };
    public static final String[] BUSINESS_TYPES = {
            "Wholesaler", "Retailer", "Exporter", "Processor", "Distributor"
    };
    public static final String[] ALERT_TYPES = {
            "Heavy Rain", "Flood", "Storm", "Drought", "Heatwave", "Cold Wave"
    };
    public static final String[] SEVERITY_LEVELS = {
            "Low", "Medium", "High", "Critical"
    };
    public static final String[] DISEASE_SEVERITY = {
            "Low", "Medium", "High", "Critical"
    };
    public static final String[] SOIL_HEALTH_STATUS = {
            "Excellent", "Good", "Fair", "Poor"
    };
    public static final String LANG_BENGALI = "Bengali";
    public static final String LANG_ENGLISH = "English";
    public static final String[] LANGUAGES = { LANG_BENGALI, LANG_ENGLISH };
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String CURRENCY = "BDT";
    public static final String CURRENCY_SYMBOL = "৳";
    public static final String[] UNITS = { "kg", "quintal", "ton", "piece", "dozen" };
    public static final String TABLE_FARMERS = "farmers";
    public static final String TABLE_BUYERS = "buyers";
    public static final String TABLE_CROPS = "crops";
    public static final String TABLE_MARKET_PRICES = "market_prices";
    public static final String TABLE_SOIL_HEALTH = "soil_health";
    public static final String TABLE_CROP_DISEASES = "crop_diseases";
    public static final String TABLE_WEATHER_ALERTS = "weather_alerts";
    public static final String WEATHER_API_URL = "https://api.weatherapi.com/v1";
    public static final String SMS_GATEWAY_URL = "https://api.smsgateway.bd/send";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final String PRIMARY_COLOR = "#2E7D32";
    public static final String SECONDARY_COLOR = "#FFA726";
    public static final String PHONE_PATTERN = "^(\\+880|880)?[1][3-9]\\d{8}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;

    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
