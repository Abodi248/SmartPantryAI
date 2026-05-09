package com.example.smartpantry.model;

public class ChatMessage {

    public enum Role { USER, ASSISTANT }

    private final String text;
    private final Role role;
    private final long timestampMs;

    public ChatMessage(String text, Role role) {
        this.text = text;
        this.role = role;
        this.timestampMs = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public Role getRole() { return role; }
    public long getTimestampMs() { return timestampMs; }
    public boolean isUser() { return role == Role.USER; }
}