package com.lol.ml.starthackapi;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
=======
import org.springframework.scheduling.annotation.Scheduled;
>>>>>>> 8a93245564068652ed412c4a68f4d9463240a6bf
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
<<<<<<< HEAD
import com.lol.ml.starthackapi.service.*;
import org.json.JSONObject;
import java.util.concurrent.*;

@Component
public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private ConversationProcessor conversationProcessor;

    @Autowired
    private ClientService clientService;

    @Autowired
    private PromptApiRepo geminiRepo;

    @Autowired
    private SixRepo sixRepo;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        // Send mock client info
        JSONObject clientInfo = new JSONObject(clientService.getClientById("client1"));
        session.sendMessage(new TextMessage(clientInfo.toString()));

        // Schedule conversation checking
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndProcessConversation(sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
=======
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Component
public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private static WebSocketSession session = null;
    PromptApiRepo promptApiRepo = new PromptApiRepo();

    private String textMessage = "";
    private String voiceMessage = "";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LiveTextWebSocketHandler.session = session;
        System.out.println("Connection to session " + session.getId() + " established.");

>>>>>>> 8a93245564068652ed412c4a68f4d9463240a6bf
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
<<<<<<< HEAD
        String sessionId = session.getId();
        String word = message.getPayload();

        // Process word by word
        conversationProcessor.addWord(sessionId, word);
    }

    private void checkAndProcessConversation(String sessionId) throws Exception {
        if (conversationProcessor.shouldProcessConversation(sessionId)) {
            String conversation = conversationProcessor.getConversation(sessionId);

            if (!conversation.isEmpty()) {
                String response;
                if (conversationProcessor.isFinancialQuery(conversation)) {
                    // Use SIX API for financial queries
                    response = sixRepo.getResponse(conversation);
                } else {
                    // Use Gemini API for other queries
                    response = String.join("\n", geminiRepo.callGeminiAPI(conversation));
                }

                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(response));
                }

                // Clear the conversation after processing
                conversationProcessor.clearConversation(sessionId);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        conversationProcessor.clearConversation(sessionId);
=======
        String payload = message.getPayload();

        //String json = "{\"channel\": \"textchannel\", \"content\": \"textcontent\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);

        String channel = jsonNode.get("channel").asText();
        String content = jsonNode.get("content").asText();

        if(channel.equals("text")) {
            textMessage = textMessage + " " + content;
        } else {
            voiceMessage = voiceMessage + " " + content;
        }
        System.out.println("62"  + jsonNode + " " +textMessage + " " + voiceMessage + " " + channel + " " + content);



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
>>>>>>> 8a93245564068652ed412c4a68f4d9463240a6bf
    }

    @Scheduled(fixedRate = 15000, initialDelay = 15000)
    private void returnPrediction (){
        if(session == null || !session.isOpen()){
            return;
        }

        String toGemini = "We would like you to make a Prediction of the next Question our customer could" +
                "ask us. If possible it should be in the financial context. Please keep it as shortly and precisely" +
                "as possible. Here is a snipped of our last conversation: " + voiceMessage;

        voiceMessage = "";

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
