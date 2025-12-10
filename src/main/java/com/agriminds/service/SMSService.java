package com.agriminds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SMSService {
    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);
    private static final String SMS_API_URL = "https://api.smsgateway.bd/send";
    private static final String API_KEY = "your_sms_api_key_here";
    private static final String SENDER_ID = "AgriMinds";

    public boolean sendSMS(String phoneNumber, String message) {
        try {
            String formattedPhone = formatPhoneNumber(phoneNumber);
            URL url = new URL(SMS_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            String jsonPayload = String.format(
                    "{\"sender\":\"%s\",\"recipient\":\"%s\",\"message\":\"%s\"}",
                    SENDER_ID, formattedPhone, escapeJson(message));
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                logger.info("SMS sent successfully to: {}", phoneNumber);
                return true;
            } else {
                logger.error("Failed to send SMS. Response code: {}", responseCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error sending SMS", e);
            return false;
        }
    }

    public int sendBulkSMS(String[] phoneNumbers, String message) {
        int successCount = 0;
        for (String phone : phoneNumbers) {
            if (sendSMS(phone, message)) {
                successCount++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Bulk SMS sent: {} out of {} successful", successCount, phoneNumbers.length);
        return successCount;
    }

    public boolean sendPriceAlert(String phoneNumber, String cropName, double price, String market) {
        String message = String.format(
                "AgriMinds Price Alert: %s at %s market is now ৳%.2f per kg. Visit AgriMinds for more details.",
                cropName, market, price);
        return sendSMS(phoneNumber, message);
    }

    public boolean sendWeatherAlert(String phoneNumber, String alertType, String description) {
        String message = String.format(
                "AgriMinds Weather Alert: %s - %s. Take necessary precautions for your crops.",
                alertType, description);
        return sendSMS(phoneNumber, message);
    }

    public boolean sendDiseaseAlert(String phoneNumber, String cropName, String diseaseName) {
        String message = String.format(
                "AgriMinds Crop Alert: %s disease detected in %s. Check app for treatment recommendations.",
                diseaseName, cropName);
        return sendSMS(phoneNumber, message);
    }

    private String formatPhoneNumber(String phone) {
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("880")) {
            phone = phone.substring(3);
        }
        if (!phone.startsWith("1")) {
            phone = "1" + phone;
        }
        return phone;
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
