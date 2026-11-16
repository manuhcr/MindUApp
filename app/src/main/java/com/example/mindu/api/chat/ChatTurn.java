
package com.example.mindu.api.chat;

public class ChatTurn {

    private String role;
    private String content;

    public ChatTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
