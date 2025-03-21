package com.lol.ml.starthackapi;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class SixRepo {



    private static String SIX_GPT_URL = "https://idchat-api-containerapp01-dev.orangepebble-16234c4b."
            + "switzerlandnorth.azurecontainerapps.io/query?query=";

    private static final OkHttpClient client = new OkHttpClient();

    public String getResponse(String query) {
        SIX_GPT_URL+= query;
        // Create JSON body to send with the query
        // Convert the JSON body to string
        RequestBody body = RequestBody.create("", MediaType.get("application/json"));

        // Build the POST request with the appropriate Content-Type header
        Request request = new Request.Builder()
                .url(SIX_GPT_URL)
                .post(body)  // Use POST method
                .header("Content-Type", "application/json")  // Ensure content type is JSON
                .build();

        // Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Error: SIX API returned " + response.code() + " - " + response.body().string();
            }

            // Parse the JSON response
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Extract the "messages" key from the response
            JSONArray messages = jsonObject.optJSONArray("messages");
            if (messages != null && messages.length() > 1) {
                // Access the second message where the AI response is expected
                JSONObject aiMessage = messages.getJSONObject(1);
                String aiContent = aiMessage.optString("content", "No AI response found.");

                return aiContent;  // Return AI content from the response
            }

            return "No valid AI response found.";

        } catch (IOException e) {
            return "Error contacting SIX GPT API: " + e.getMessage();
        }
    }
}
