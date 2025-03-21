package com.lol.ml.starthackapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auth.oauth2.GoogleAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.lol.ml.starthackapi.service.*;
import org.json.JSONObject;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@EnableScheduling
@Component
public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private static WebSocketSession session = null;
    PromptApiRepo promptApiRepo = new PromptApiRepo();

    private static final AtomicReference<String> textMessage = new AtomicReference<>("");
    private static final AtomicReference<String> voiceMessage = new AtomicReference<>("");

    private ConversationProcessor conversationProcessor = new ConversationProcessor();

    private ClientService clientService = new ClientService();

    private PromptApiRepo geminiRepo = new PromptApiRepo();

    private SixRepo sixRepo = new SixRepo();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LiveTextWebSocketHandler.session = session;
        System.out.println("Connection to session " + session.getId() + " established.");




    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //String sessionId = session.getId();
        String word = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(word);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String channel = jsonNode.get("channel").asText();
        String content = jsonNode.get("content").asText();
        //TODO change addWOrd for conversation because we get whole sentence
        // Process word by word
        //conversationProcessor.addWord(sessionId, word);

        if(channel.equals("text")) {
            checkAndProcessConversation(content);
        } else {
            voiceMessage.updateAndGet(v -> v + " " + content);
        }

        System.out.println("62"  + jsonNode + "textMe " +textMessage + "Voice " + voiceMessage + " " + channel + " " + content);
    }

//    private void checkAndProcessConversation(String message) throws Exception {
//        String sessionId = session.getId();
//
//        System.out.println("100");
//
//        if (conversationProcessor.shouldProcessConversation(sessionId)) {
//            System.out.println("101");
//            String conversation = conversationProcessor.getConversation(sessionId);
//
//            if (!conversation.isEmpty()) {
//                System.out.println("109" + conversation);
//
//                String response;
//                if (conversationProcessor.isFinancialQuery(conversation)) {
//                    // Use SIX API for financial queries
//                    response = sixRepo.getResponse(conversation);
//                    System.out.println("109");
//
//                } else {
//                    System.out.println("110");
//
//                    // Use Gemini API for other queries
//                    response = String.join("\n", geminiRepo.callGeminiAPI(conversation));
//                    System.out.println("118" + response);
//
//                }
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonString;
//                System.out.println("120");
//
//                try {
//                    System.out.println("123");
//
//                    jsonString = objectMapper.writeValueAsString(Map.of("content", response, "tag", "chat"));
//                    System.out.println(jsonString);
//                    sendMessageToClient(jsonString);
//                } catch (IOException e) {
//                    System.out.println(133 + e.getMessage());
//                }
//
//
//
//                // Clear the conversation after processing
//                conversationProcessor.clearConversation(sessionId);
//            }
//        }
//    }

    private void checkAndProcessConversation (String message){
        System.out.println("Una12ble312 to assist");
        String rawResponse;
        String response;

        rawResponse = sixRepo.getResponse(message);

        if(rawResponse.contains("unable") || rawResponse.contains("assist") || rawResponse.contains("queries") || rawResponse.contains("Error")){
            System.out.println("Unable to assist");
            String toGemini = "You are chatting with a Wealth Manager, so please only answer the following " +
                    "prompt only if it has something to do with finances. If not you should always " +
                    "respond: I am sorry. I cannot help you with that. I am trained to assist a wealth manager " +
                    "with financial questions. -> Following you have my prompt: " + message;
            response = String.join(" ", geminiRepo.callGeminiAPI(toGemini));
        } else {
            response = rawResponse.substring(0,10);
        }

        System.out.println("Una12ble to assist");


        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            System.out.println("Unable 412to assist" + objectMapper);

            jsonString = objectMapper.writeValueAsString(Map.of("content", response, "tag", "chat"));
            System.out.println(jsonString);
            sendMessageToClient(jsonString);
            System.out.println("Unable to assis141t" + jsonString);

        } catch (IOException e) {
            System.out.println("Unable to ass2ist" + e.getMessage());

        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        System.out.println("Connection to session " + sessionId + " closed.");
        conversationProcessor.clearConversation(sessionId);
        LiveTextWebSocketHandler.session = null;
    }



    public void sendMessageToClient(String message) throws IOException {
        if (session != null && session.isOpen()) {
            System.out.println("Unable to send message");

            session.sendMessage(new TextMessage(message));
        }
    }

    @Scheduled(fixedRate = 10000)
    private void returnClient(){
        if(session == null || !session.isOpen()){
            return;
        }

        String name = "David Lang";
        String age = "37";
        String description;

        int randomNumber = new Random().nextInt(3) + 1;

        if(randomNumber == 1){
            description = "Invested in Google";
        } else if (randomNumber == 2) {
            description = "Likes BioTech";
        } else {
            description = "FCLiverpool Fan";
        }

        String combinedString = String.join("|", List.of(name, age, description));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(Map.of("content", combinedString, "tag", "client"));
            System.out.println(jsonString);
            sendMessageToClient(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 15000, initialDelay = 15000)
    private void returnPrediction (){
        if(session == null || !session.isOpen()){
            return;
        }

        String currentVoiceMessage = voiceMessage.getAndSet(""); // Get and reset safely

        System.out.println("voice: " + currentVoiceMessage);

        String toGemini = "You are chatting with a Wealth Manager, so please only answer the following " +
                "prompt only if it has something to do with finances. If not you should always respond: " +
                "Gathering Information. -> We would like " +
                "you to make a Prediction of the next Question our customer could" +
                " ask us. Please keep it as short and precise" +
                " as possible and also provide a short and precise answer. Please do not use any emojis or" +
                "square/curly braces and start the question with Q: and the answer with A: " +
                "-> Here is a snippet of our last conversation: " + currentVoiceMessage;

        List<String> geminiResponse = promptApiRepo.callGeminiAPI(toGemini);

        String combinedString = String.join(" ", geminiResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(Map.of("content", combinedString, "tag", "prediction"));
            System.out.println(jsonString);
            sendMessageToClient(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
