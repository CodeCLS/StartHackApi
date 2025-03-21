package com.lol.ml.starthackapi.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ConversationProcessor {
    private static final Set<String> FINANCIAL_KEYWORDS = new HashSet<>(Arrays.asList(
            "investment", "stock", "bond", "market", "portfolio", "fund",
            "dividend", "interest", "rate", "bank", "money", "finance",
            "trading", "savings", "retirement", "pension"));

    private Map<String, String> activeConversations = new HashMap<>();
    private Map<String, Long> lastMessageTime = new HashMap<>();
    private static final long PAUSE_THRESHOLD = 10000; // 10 seconds in milliseconds

    public void addWord(String sessionId, String word) {
        activeConversations.put(sessionId, word);
        lastMessageTime.put(sessionId, System.currentTimeMillis());
    }

    public boolean shouldProcessConversation(String sessionId) {
        Long lastTime = lastMessageTime.get(sessionId);
        return lastTime != null &&
                (System.currentTimeMillis() - lastTime) >= PAUSE_THRESHOLD;
    }

    public String getConversation(String sessionId) {
        String conv = activeConversations.get(sessionId);
        return conv != null ? conv : "";
    }

    public boolean isFinancialQuery(String text) {
        String lowerText = text.toLowerCase();
        return FINANCIAL_KEYWORDS.contains(lowerText);
    }

    public void clearConversation(String sessionId) {
        activeConversations.remove(sessionId);
        lastMessageTime.remove(sessionId);
    }
}