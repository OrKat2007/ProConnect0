package com.example.proconnect.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class ChatModel {
    private String chatId;
    private String user1;
    private Object createdAt;                // Allows both Long and Timestamp
    private String lastMessage;             // Optional preview of last message
    private Object lastMessageTimestamp;    // Firestore field for last message time
    private List<String> participants;      // New: array of user1 and otherUser

    // Partner details loaded from Firestore
    private String otherUserName;
    private String otherUserImage;
    private String otherUserUid;
    private String professional;
    private String location;
    private String dob;
    private String languages;
    private String availability;

    public ChatModel() { }

    // chatId
    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    // user1 (current user)
    public String getUser1() {
        return user1;
    }
    public void setUser1(String user1) {
        this.user1 = user1;
    }

    // createdAt
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

    // lastMessage
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    // lastMessageTimestamp
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

    // participants
    public List<String> getParticipants() {
        return participants;
    }
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    // Partner fields
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

    public String getDob() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob = dob;
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
}