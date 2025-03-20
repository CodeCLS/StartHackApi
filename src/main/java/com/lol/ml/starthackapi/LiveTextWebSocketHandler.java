package com.lol.ml.starthackapi;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class LiveTextWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionID = session.getId();
        sessions.put(sessionID,session);
        System.out.println("Connection to session " + sessionID + "established.");
        session.sendMessage(new TextMessage("Hello Client!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received: " + payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionID = session.getId();
        sessions.remove(sessionID);
        System.out.println("Connection to session " + sessionID + "closed.");
    }

    public void sendMessageToClient(String sessionID, String message) throws IOException {
        WebSocketSession session = sessions.get(sessionID);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}
