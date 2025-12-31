package com.agriminds.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * AI Service using Google Gemini API for instant agricultural advice
 * Gemini provides free tier with 60 requests per minute
 */
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    // Using gemini-2.5-flash (stable June 2025 release)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final boolean USE_FALLBACK = false; // Set to false when API is working
    private String apiKey;
    private final Gson gson;

    public AIService() {
        this.gson = new Gson();
        loadApiKey();
    }

    /**
     * Load Gemini API key from configuration
     */
    private void loadApiKey() {
        try {
            Properties properties = new Properties();
            // Load from classpath instead of file path
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            this.apiKey = properties.getProperty("gemini.api.key", "");

            System.out.println("=== API Key Loading ===");
            System.out.println("API Key length: " + (apiKey != null ? apiKey.length() : "NULL"));
            System.out.println("API Key (first 10 chars): "
                    + (apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : apiKey));

            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("Gemini API key not configured. AI features will be disabled.");
                logger.info("Get free API key from: https://makersuite.google.com/app/apikey");
                System.out.println("WARNING: No API key found in config.properties");
            } else {
                logger.info("Gemini AI Service initialized successfully");
                System.out.println("SUCCESS: Gemini AI Service initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to load API configuration", e);
        }
    }

    /**
     * Get AI-powered answer for farmer's agricultural question
     * 
     * @param category     Question category (Disease, Pest, Soil, Weather, etc.)
     * @param questionText The farmer's question
     * @return AI-generated answer with agricultural expertise
     */
    public String getAIAnswer(String category, String questionText) {
        System.out.println("=== AI Service Called ===");
        System.out.println("Category: " + category);
        System.out.println("Question: " + questionText);
        System.out.println("API Key configured: " + (apiKey != null && !apiKey.isEmpty()));

        // Use fallback if API is blocked by firewall/network
        if (USE_FALLBACK) {
            System.out.println("Using fallback mock response (API disabled)");
            return getFallbackAnswer(category, questionText);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("AI service unavailable - no API key configured");
            System.out.println("ERROR: No API key configured");
            return "AI service is currently unavailable. An expert will answer your question soon.";
        }

        try {
            String prompt = buildAgriculturePrompt(category, questionText);
            System.out.println("Calling Gemini API...");
            String answer = callGeminiAPI(prompt);

            System.out.println("AI Response received: "
                    + (answer != null ? answer.substring(0, Math.min(100, answer.length())) + "..." : "NULL"));

            if (answer != null && !answer.isEmpty()) {
                logger.info("Successfully generated AI answer for question in category: {}", category);
                return answer;
            } else {
                System.out.println("ERROR: Empty or null answer from AI");
                return "Unable to generate AI response at the moment. An expert will review your question.";
            }
        } catch (Exception e) {
            logger.error("Error getting AI answer", e);
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            return "Unable to generate AI response. An expert will answer your question soon.";
        }
    }

    /**
     * Build specialized prompt for agricultural questions in Bangladesh context
     */
    private String buildAgriculturePrompt(String category, String questionText) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are an expert agricultural advisor with deep knowledge of farming practices in Bangladesh. ");
        prompt.append("You provide practical, actionable advice to help farmers succeed.\n\n");

        prompt.append("Question Category: ").append(category).append("\n");
        prompt.append("Farmer's Question: ").append(questionText).append("\n\n");

        prompt.append("Please provide a comprehensive answer considering:\n");
        prompt.append("1. Bangladesh's tropical monsoon climate and soil conditions\n");
        prompt.append("2. Local crop varieties (rice, jute, tea, vegetables common in Bangladesh)\n");
        prompt.append("3. Cost-effective solutions for small-scale farmers\n");
        prompt.append("4. Traditional and modern farming techniques\n");
        prompt.append("5. Seasonal considerations (Kharif, Rabi seasons)\n");
        prompt.append("6. Safety, sustainability, and environmental impact\n\n");

        prompt.append("Structure your answer with:\n");
        prompt.append("• Problem Analysis: What is the issue?\n");
        prompt.append("• Immediate Actions: What should the farmer do right away?\n");
        prompt.append("• Detailed Recommendations: Step-by-step solutions\n");
        prompt.append("• Prevention Tips: How to avoid this in the future\n");
        prompt.append("• Warning Signs: When to seek additional expert help\n\n");

        prompt.append("Use simple, clear language. Keep the answer between 200-400 words. ");
        prompt.append(
                "Be specific with measurements, timing, and methods. Include both Bengali and English names for crops/chemicals where relevant.");

        return prompt.toString();
    }

    /**
     * Call Google Gemini API to generate response
     */
    private String callGeminiAPI(String prompt) throws IOException {
        String requestUrl = GEMINI_API_URL + "?key=" + apiKey;

        // Create HTTP client with timeout settings (longer for 2048 token responses)
        org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig
                .custom()
                .setConnectTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(10))
                .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofSeconds(60))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpPost request = new HttpPost(requestUrl);
            request.setHeader("Content-Type", "application/json");
            request.setConfig(requestConfig);

            // Build Gemini API request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Add generation config for better responses
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("maxOutputTokens", 8192);
            requestBody.add("generationConfig", generationConfig);

            // Add safety settings to allow agricultural content
            JsonArray safetySettings = new JsonArray();
            String[] categories = { "HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH",
                    "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT" };
            for (String cat : categories) {
                JsonObject setting = new JsonObject();
                setting.addProperty("category", cat);
                setting.addProperty("threshold", "BLOCK_ONLY_HIGH");
                safetySettings.add(setting);
            }
            requestBody.add("safetySettings", safetySettings);

            // Set request entity
            StringEntity entity = new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8);
            request.setEntity(entity);

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("=== Gemini API Response ===");
                System.out.println("Status Code: " + response.getCode());
                System.out.println("Response Body: " + responseBody);
                System.out.println("===========================");
                return parseGeminiResponse(responseBody);
            } catch (org.apache.hc.core5.http.ParseException e) {
                logger.error("Failed to parse HTTP response", e);
                System.out.println("ERROR: ParseException - " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Parse Gemini API response and extract the generated text
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            System.out.println("=== Parsing Gemini Response ===");
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Check for error
            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                String errorMsg = error.get("message").getAsString();
                int errorCode = error.get("code").getAsInt();

                System.out.println("ERROR from Gemini API:");
                System.out.println("Code: " + errorCode);
                System.out.println("Message: " + errorMsg);

                logger.error("Gemini API error ({}): {}", errorCode, errorMsg);

                if (errorCode == 400) {
                    return "Invalid question format. Please rephrase and try again.";
                } else if (errorCode == 429) {
                    return "Too many requests. Please try again in a moment.";
                } else if (errorCode == 403) {
                    return "API key error. Please contact support.";
                }
                return null;
            }

            // Extract the generated content
            if (jsonResponse.has("candidates")) {
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();

                    // Check for safety blocks
                    if (candidate.has("finishReason")) {
                        String finishReason = candidate.get("finishReason").getAsString();
                        if ("SAFETY".equals(finishReason)) {
                            logger.warn("Response blocked by safety filters");
                            return "Unable to generate response due to content filters. An expert will answer.";
                        }
                    }

                    JsonObject contentObj = candidate.getAsJsonObject("content");
                    JsonArray partsArray = contentObj.getAsJsonArray("parts");
                    if (partsArray.size() > 0) {
                        String answer = partsArray.get(0).getAsJsonObject().get("text").getAsString();
                        return answer.trim();
                    }
                }
            }

            logger.warn("Unexpected Gemini API response format");
            return null;

        } catch (Exception e) {
            logger.error("Error parsing Gemini response", e);
            return null;
        }
    }

    /**
     * Fallback method providing mock agricultural answers for testing
     * Use this when Gemini API is blocked by firewall/network
     */
    private String getFallbackAnswer(String category, String questionText) {
        String question = questionText.toLowerCase();

        // Disease-related questions
        if (category.contains("Disease") || category.contains("রোগ")) {
            if (question.contains("black spot") || question.contains("কালো দাগ")) {
                return "**Problem Analysis:**\n" +
                        "Black spots on leaves are typically caused by fungal diseases like Black Spot Disease (Cercospora leaf spot) or bacterial leaf blight. Common in Bangladesh's humid climate.\n\n"
                        +
                        "**Immediate Actions:**\n" +
                        "• Remove and destroy affected leaves immediately\n" +
                        "• Isolate infected plants if possible\n" +
                        "• Avoid overhead watering to reduce moisture on leaves\n\n" +
                        "**Detailed Recommendations:**\n" +
                        "1. Apply copper-based fungicide (Bordeaux mixture) - Mix 1kg copper sulfate + 1kg lime in 100L water\n"
                        +
                        "2. Spray neem oil solution (5ml neem oil per liter of water) weekly\n" +
                        "3. Ensure proper spacing between plants for air circulation\n" +
                        "4. Apply organic mulch to prevent soil splash on leaves\n" +
                        "5. Maintain soil pH between 6.0-7.0\n\n" +
                        "**Prevention Tips:**\n" +
                        "• Use disease-resistant varieties\n" +
                        "• Practice crop rotation (don't plant same family crops consecutively)\n" +
                        "• Water plants at soil level, avoid wetting foliage\n" +
                        "• Apply balanced NPK fertilizer (avoid excess nitrogen)\n\n" +
                        "**Warning Signs:**\n" +
                        "Seek expert help if spots spread rapidly, leaves turn yellow and drop, or entire plant wilts despite treatment.";
            }
            if (question.contains("white spot") || question.contains("সাদা দাগ")) {
                return "**Problem Analysis:**\n" +
                        "White spots on leaves indicate powdery mildew or white rust disease. Very common during Bangladesh's winter season (Rabi) when humidity is high.\n\n"
                        +
                        "**Immediate Actions:**\n" +
                        "• Prune affected leaves and burn them\n" +
                        "• Reduce irrigation frequency\n" +
                        "• Increase plant spacing for better air flow\n\n" +
                        "**Detailed Recommendations:**\n" +
                        "1. Spray sulfur-based fungicide (wettable sulfur 80% WP) - 2g per liter of water\n" +
                        "2. Apply baking soda solution: Mix 1 tablespoon baking soda + 2.5 tablespoons vegetable oil in 1 gallon water\n"
                        +
                        "3. Spray milk solution (1 part milk to 9 parts water) every 3 days\n" +
                        "4. Apply potassium bicarbonate fungicide for organic treatment\n" +
                        "5. Treat early morning or evening to avoid leaf burn\n\n" +
                        "**Prevention Tips:**\n" +
                        "• Select resistant varieties (check with local agricultural office)\n" +
                        "• Avoid overcrowding plants\n" +
                        "• Remove plant debris regularly\n" +
                        "• Maintain good drainage in fields\n\n" +
                        "**Warning Signs:**\n" +
                        "If white coating spreads to entire plant, fruits develop spots, or growth stunts significantly, consult agricultural extension officer immediately.";
            }
            // Default disease answer
            return "**Problem Analysis:**\n" +
                    "Plant diseases in Bangladesh are often caused by fungi, bacteria, or viruses, exacerbated by high humidity and temperature.\n\n"
                    +
                    "**Immediate Actions:**\n" +
                    "• Identify the exact symptoms (spots, wilting, discoloration)\n" +
                    "• Remove severely affected plant parts\n" +
                    "• Quarantine infected plants if possible\n\n" +
                    "**Detailed Recommendations:**\n" +
                    "1. Apply broad-spectrum fungicide like Mancozeb (2g/L water)\n" +
                    "2. Use neem-based organic pesticides for mild infections\n" +
                    "3. Improve drainage and reduce excess moisture\n" +
                    "4. Ensure proper nutrition with balanced fertilizer\n\n" +
                    "**Prevention Tips:**\n" +
                    "• Practice crop rotation annually\n" +
                    "• Use certified disease-free seeds\n" +
                    "• Maintain field hygiene\n" +
                    "• Monitor plants weekly for early detection\n\n" +
                    "**Warning Signs:**\n" +
                    "Contact agricultural extension office if disease spreads rapidly or affects large crop areas.";
        }

        // Pest-related questions
        if (category.contains("Pest") || category.contains("পোকা")) {
            return "**Problem Analysis:**\n" +
                    "Pest infestation is common in Bangladesh's agricultural system. Identifying the specific pest is crucial for effective treatment.\n\n"
                    +
                    "**Immediate Actions:**\n" +
                    "• Manually remove visible pests\n" +
                    "• Set up yellow sticky traps to monitor population\n" +
                    "• Inspect undersides of leaves daily\n\n" +
                    "**Detailed Recommendations:**\n" +
                    "1. Spray neem oil solution (5ml/L water) every 7 days\n" +
                    "2. Use soap water spray (5ml liquid soap in 1L water) for soft-bodied insects\n" +
                    "3. Apply biological control: Introduce natural predators (ladybugs for aphids)\n" +
                    "4. Use pheromone traps for specific pests\n" +
                    "5. Chemical control: Apply recommended insecticide only if infestation is severe\n\n" +
                    "**Prevention Tips:**\n" +
                    "• Intercrop with pest-repellent plants (marigold, basil)\n" +
                    "• Maintain field cleanliness\n" +
                    "• Use mulching to reduce soil-dwelling pests\n" +
                    "• Rotate crops to break pest life cycles\n\n" +
                    "**Warning Signs:**\n" +
                    "Severe leaf damage, stunted growth, or fruit drop indicates need for professional pest management advice.";
        }

        // Default general answer
        return "**Agricultural Advice:**\n" +
                "Thank you for your question about " + category + ". Here's expert guidance:\n\n" +
                "**Analysis:**\n" +
                "Agricultural challenges in Bangladesh require localized solutions considering our climate, soil type, and traditional practices.\n\n"
                +
                "**Recommendations:**\n" +
                "1. Consult with your local agricultural extension officer for specific guidance\n" +
                "2. Test your soil to understand nutrient requirements\n" +
                "3. Use quality inputs (seeds, fertilizers) from authorized dealers\n" +
                "4. Follow integrated pest management (IPM) practices\n" +
                "5. Keep records of farming activities for better decision-making\n\n" +
                "**Best Practices:**\n" +
                "• Maintain proper irrigation schedule based on crop requirements\n" +
                "• Apply organic matter to improve soil health\n" +
                "• Monitor crops regularly for early problem detection\n" +
                "• Join farmer groups to share knowledge and resources\n\n" +
                "**Resources:**\n" +
                "Contact Bangladesh Agricultural Development Corporation (BADC) or Department of Agricultural Extension (DAE) for detailed technical support.\n\n"
                +
                "Note: This is AI-generated advice. For complex issues, please consult agricultural experts.";
    }

    /**
     * Test the AI service connection
     * 
     * @return true if API is working, false otherwise
     */
    public boolean testConnection() {
        try {
            String testResponse = getAIAnswer("General", "What is the best season for rice cultivation?");
            return testResponse != null && !testResponse.contains("unavailable");
        } catch (Exception e) {
            logger.error("AI service test failed", e);
            return false;
        }
    }
}
