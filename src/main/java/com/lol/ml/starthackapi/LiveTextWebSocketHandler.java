package com.lol.ml.starthackapi;

import com.fasterxml.jackson.core.JsonProcessingException;
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
@EnableScheduling

@Component
public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private static WebSocketSession session = null;
    PromptApiRepo promptApiRepo = new PromptApiRepo();

    private String textMessage = "";
    private String voiceMessage = "";

    private ConversationProcessor conversationProcessor = new ConversationProcessor();

    private ClientService clientService = new ClientService();

    private PromptApiRepo geminiRepo = new PromptApiRepo();

    private SixRepo sixRepo = new SixRepo();
    String sessionId = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        sessionId = session.getId();
        System.out.println("Connection to session " + session.getId() + " established.");



        // Send mock client info
        JSONObject clientInfo = new JSONObject(clientService.getClientById("client1"));
        session.sendMessage(new TextMessage(clientInfo.toString()));


        //


        //String json = "{\"channel\": \"textchannel\", \"content\": \"textcontent\"}";




    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String word = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(word);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String channel = jsonNode.get("channel").asText();
        String content = jsonNode.get("content").asText();

        // Process word by word
        conversationProcessor.addWord(sessionId, word);

        if(channel.equals("text")) {
            textMessage = textMessage + " " + content;
        } else {
            voiceMessage = voiceMessage + " " + content;
        }

        System.out.println("62"  + jsonNode + " " +textMessage + " " + voiceMessage + " " + channel + " " + content);



        returnChat();


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

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString;
                try {
                    jsonString = objectMapper.writeValueAsString(Map.of("content", response, "tag", "chat"));
                    System.out.println(jsonString);
                    sendMessageToClient(jsonString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }



                // Clear the conversation after processing
                conversationProcessor.clearConversation(sessionId);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Connection to session " + sessionId + " closed.");
        LiveTextWebSocketHandler.session = null;
        conversationProcessor.clearConversation(sessionId);
        sessionId = null;

    }



    public void sendMessageToClient(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
    private void returnChat(){
        try {
            checkAndProcessConversation(sessionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
