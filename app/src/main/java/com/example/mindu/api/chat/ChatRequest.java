package com.example.mindu.api.chat;

import java.util.List;

public class ChatRequest {

    private String message;
    private List<ChatTurn> history;

    public ChatRequest(String message, List<ChatTurn> history) {
        this.message = message;
        this.history = history;
    }

    public String getMessage() {
        return message;
    }

    public List<ChatTurn> getHistory() {
        return history;
    }
}
