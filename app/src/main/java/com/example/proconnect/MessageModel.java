package com.example.proconnect;

public class MessageModel {
    private String sender;
    private String text;
    private long timestamp;

    public MessageModel() {} // Firestore requires an empty constructor

    public MessageModel(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }
    public String getText() {
        return text;
    }
    public long getTimestamp() {
        return timestamp;
    }
}
