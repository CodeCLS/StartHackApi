package com.lol.ml.starthackapi;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PromptApiRepo {

    @Value("${gemini.api.key}")  // Load API key from application.properties
    private String apiKey = "AIzaSyDPxUmsa3vR7PMEaeG0abwGI6YI_MSMq9Q";

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final OkHttpClient client = new OkHttpClient();

    public List<String> callGeminiAPI(String text) {
        String fullUrl = GEMINI_URL + "?key=" + apiKey;

        // JSON request body (correct format)
        String jsonBody = "{"
                + "\"contents\": [ { \"parts\": [ { \"text\": \"" + text + "\" } ] } ],"
                + "\"generationConfig\": { \"maxOutputTokens\": 50 }"
                + "}";

        RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();
        System.out.println("36I");


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("41I");

                throw new RuntimeException("API Error: " + response.code() + " - " + response.message());
            }

            // Parse JSON response
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            List<String> outputChunks = new ArrayList<>();
            System.out.println("50I");

            // Extract AI-generated text
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                System.out.println("58I");

                if (parts.length() > 0) {
                    System.out.println("41I");

                    String aiResponse = parts.getJSONObject(0).getString("text");

                    // Split response into sentences for chunking
                    String[] chunks = aiResponse.split("(?<=\\. )");
                    StringBuilder output = new StringBuilder();
                    for (String chunk : chunks) {
                        chunk = chunk.trim();
                        output.append(chunk);

                    }
                    System.out.println("80I");

                    String importance = determineImportance("");
                    String formattedChunk = "{type: \"text\", importance: \"" + importance + "\", content: \"" + output.toString() + "\"}";
                    outputChunks.add(formattedChunk);
                }
            }

            return outputChunks;
        } catch (Exception e) {
            System.out.println("51I " + e.getMessage());

            throw new RuntimeException("Error calling Gemini API: " + e.getMessage());
        }
    }

    // Function to determine importance level based on content length or keywords
    private String determineImportance(String chunk) {
        if (chunk.length() > 100 || chunk.contains("key aspect") || chunk.contains("important")) {
            return "very";
        } else if (chunk.length() > 50) {
            return "medium";
        } else {
            return "low";
        }
    }
}
