package com.lol.ml.starthackapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
@SpringBootApplication
public class StartHackApiApplication {

    private final PromptApiRepo geminiRepository;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public StartHackApiApplication(PromptApiRepo geminiRepository) {
        this.geminiRepository = geminiRepository;

    }

    public static void main(String[] args) {
        SpringApplication.run(StartHackApiApplication.class, args);
    }

    //public static void main(String[] args) {
    //    SpringApplication app = new SpringApplication(StartHackApiApplication.class);
    //    Map<String, Object> properties = new HashMap<>();
//
    //    String port = System.getenv("PORT"); // Get Heroku's assigned port
    //    if (port == null) {
    //        port = "8080"; // Default for local testing
    //    }
//
    //    System.out.println("⚡ Star ting application on port: " + port);
    //    properties.put("server.port", port);
    //    app.setDefaultProperties(properties);
//
    //    app.run(args);
    //}


    @GetMapping("/ask_ai")
    public ResponseEntity<String> askAi(@RequestParam String text) {
        try {
            List<String> chunks = geminiRepository.callGeminiAPI(text);

            // Combine all chunks into a single response string
            String response = String.join("\n", chunks);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/prompt_six")
    public ResponseEntity<String> promptSix(@RequestParam String query) {
        String response = new SixRepo().getResponse(query);
        return ResponseEntity.ok(response);
    }
}
