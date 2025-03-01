package com.example.proconnect.models;

import com.google.firebase.Timestamp;

public class ChatModel {
    private String chatId;
    private String user1;
    private String user2;
    private Object createdAt; // Allows both Long and Timestamp
    private String lastMessage; // Optional preview of last message
    private Object lastMessageTimestamp; // New field for last message timestamp

    // Fields to store partner details loaded from Firestore
    private String otherUserName;
    private String otherUserImage;
    private String otherUserUid;
    private String professional; // New field for professional
    private String location;     // New field for location
    private String languages;    // New field for languages
    private String availability; // New field for availability
    private Integer age;         // New field for age

    // No-argument constructor required by Firestore
    public ChatModel() { }

    public ChatModel(String user1, String user2, Object createdAt, String lastMessage) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
    }

    // Getters and setters for existing fields...
    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
    public String getUser1() {
        return user1;
    }
    public void setUser1(String user1) {
        this.user1 = user1;
    }
    public String getUser2() {
        return user2;
    }
    public void setUser2(String user2) {
        this.user2 = user2;
    }
    public Timestamp getCreatedAt() {
        if (createdAt instanceof Timestamp) {
            return (Timestamp) createdAt;
        } else if (createdAt instanceof Long) {
            return new Timestamp(((Long) createdAt) / 1000, 0);
        } else {
            return null;
        }
    }
    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    // New getter and setter for lastMessageTimestamp
    public Timestamp getLastMessageTimestamp() {
        if (lastMessageTimestamp instanceof Timestamp) {
            return (Timestamp) lastMessageTimestamp;
        } else if (lastMessageTimestamp instanceof Long) {
            return new Timestamp(((Long) lastMessageTimestamp) / 1000, 0);
        } else {
            return null;
        }
    }
    public void setLastMessageTimestamp(Object lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // New getters and setters for additional fields
    public String getOtherUserName() {
        return otherUserName;
    }
    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }
    public String getOtherUserImage() {
        return otherUserImage;
    }
    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }
    public String getOtherUserUid() {
        return otherUserUid;
    }
    public void setOtherUserUid(String otherUserUid) {
        this.otherUserUid = otherUserUid;
    }
    public String getProfessional() {
        return professional;
    }
    public void setProfessional(String professional) {
        this.professional = professional;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getLanguages() {
        return languages;
    }
    public void setLanguages(String languages) {
        this.languages = languages;
    }
    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
}
