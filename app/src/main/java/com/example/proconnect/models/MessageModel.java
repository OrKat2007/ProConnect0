package com.example.proconnect.models;

import com.google.firebase.Timestamp;

public class MessageModel {
    private String sender;
    private String text;
    private Object timestamp; // Allow both Long and Timestamp

    public MessageModel() {} // Firestore requires an empty constructor

    public MessageModel(String sender, String text, Object timestamp) {
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

    public Timestamp getTimestamp() {
        if (timestamp instanceof Timestamp) {
            return (Timestamp) timestamp;
        } else if (timestamp instanceof Long) {
            // Convert milliseconds (Long) to Firestore Timestamp
            return new Timestamp(((Long) timestamp) / 1000, 0);
        } else {
            return null; // Return null if timestamp is missing
        }
    }
}
