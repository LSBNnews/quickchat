package com.example.quickchat.model;

import java.util.List;

public class RecentChat {
    public String chatId;
    public String lastMessage;
    public long timestamp;
    public List<String> participants;

    public RecentChat(String chatId, String lastMessage, long timestamp, List<String> participants) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.participants = participants;
    }
}