package com.example.mobile_skadi.data;

public class ChatMessage {
    private long id;
    private String sessionId;
    private String sender; // "USER" or "AI"
    private String message;
    private long timestamp;
    private boolean isError;

    public ChatMessage() {
    }

    public ChatMessage(String sessionId, String sender, String message, boolean isError) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isError = isError;
    }

    public ChatMessage(long id, String sessionId, String sender, String message, long timestamp, boolean isError) {
        this.id = id;
        this.sessionId = sessionId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.isError = isError;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
