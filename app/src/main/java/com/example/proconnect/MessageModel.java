package com.example.proconnect;

public class MessageModel {
    private String sender;
    private String text;
    private com.google.firebase.Timestamp timestamp;

    public MessageModel() {} // Firestore requires an empty constructor

    public MessageModel(String sender, String text, com.google.firebase.Timestamp timestamp) {
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
    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }
}
