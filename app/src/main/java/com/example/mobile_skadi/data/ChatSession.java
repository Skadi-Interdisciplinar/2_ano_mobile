package com.example.mobile_skadi.data;

public class ChatSession {
    private String id;
    private String title;
    private long createdAt;
    private int messageCount;

    public ChatSession() {}

    public ChatSession(String id, String title, long createdAt, int messageCount) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
}
