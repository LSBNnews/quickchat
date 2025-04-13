package com.example.quickchat.model;

import java.util.List;

public class RecentChat {
    private String chatId;
    private String lastMessage;
    private long timestamp;
    private List<String> participants;

    public RecentChat() {
        // Default constructor required for calls to DataSnapshot.getValue(RecentChat.class)
    }

    public RecentChat(String chatId, String lastMessage, long timestamp, List<String> participants) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.participants = participants;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}