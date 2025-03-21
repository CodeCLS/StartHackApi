package com.lol.ml.starthackapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private volatile Instant lastMessageTime = Instant.now();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static WebSocketSession session = null;

    private String textMessage = "";
    private String voiceMessage = "";

    public LiveTextWebSocketHandler() {
        scheduler.scheduleAtFixedRate(this::checkInactivity, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LiveTextWebSocketHandler.session = session;
        System.out.println("Connection to session " + session.getId() + " established.");
        session.sendMessage(new TextMessage("Hello Client!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        lastMessageTime = Instant.now();

        //String json = "{\"channel\": \"textchannel\", \"content\": \"textcontent\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);
        System.out.println("52"  + jsonNode + " " + lastMessageTime + " " +textMessage);

        String channel = jsonNode.get("channel").asText();
        String content = jsonNode.get("content").asText();

        if(channel.equals("text")) {
            textMessage = textMessage + " " + content;
        } else {
            voiceMessage = voiceMessage + " " + content;
        }
        System.out.println("62"  + jsonNode + " " + lastMessageTime + " " +textMessage + " " + voiceMessage + " " + channel + " " + content);



    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection to session " + session.getId() + " closed.");
        LiveTextWebSocketHandler.session = null;
    }

    public void sendMessageToClient(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    @Scheduled(fixedRate = 20000)
    private void checkInactivity() {
        System.out.println("81"  + " " + lastMessageTime + " " +textMessage);

        Instant now = Instant.now();
        long secondsSinceLastMessage = Duration.between(lastMessageTime, now).getSeconds();

        if (secondsSinceLastMessage > 10) { // Falls 10 Sekunden keine Nachricht kam
            System.out.println("⚠️ Warning: No message received for " + secondsSinceLastMessage + " seconds!");

            returnPrediction(voiceMessage);

            voiceMessage = "";
        }
    }

    private void returnPrediction (String message){
        PromptApiRepo promptApiRepo = new PromptApiRepo();

        String toGemini = "We would like you to make a Prediction of the next Question our customer could" +
                "ask us. If possible it should be in the financial context. Please keep it as shortly and precisely" +
                "as possible. Here is a snipped of our last conversation: " + message;

        List<String> geminiResponse = promptApiRepo.callGeminiAPI(toGemini);
        System.out.println("103"  + " " + lastMessageTime + " " +textMessage);

        String combinedString = String.join(" ", geminiResponse);
        System.out.println("106"  + " " + lastMessageTime + " " +textMessage);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            System.out.println("111"  + " " + lastMessageTime + " " +textMessage);

            jsonString = objectMapper.writeValueAsString(Map.of("content", combinedString));
            System.out.println(jsonString);
            sendMessageToClient(jsonString);
        } catch (IOException e) {
            System.out.println("117"  + " " + lastMessageTime + " " +textMessage);

            throw new RuntimeException(e);
        }
    }
}
