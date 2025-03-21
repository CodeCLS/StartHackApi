package com.lol.ml.starthackapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
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
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
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
    }
}
