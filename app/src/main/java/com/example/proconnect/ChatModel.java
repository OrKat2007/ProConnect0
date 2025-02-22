package com.example.proconnect;

import com.google.firebase.Timestamp;

public class ChatModel {
    private String chatId;
    private String user1;
    private String user2;
    private Object createdAt; // Allows both Long and Timestamp, similar to MessageModel
    private String lastMessage; // Optional: a preview of the last message
    private String otherUserName; // New field for the display name of the other user
    private String otherUserImage;
    private String otherUserUid;

    public ChatModel() { }

    public ChatModel(String user1, String user2, Object createdAt, String lastMessage) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
    }

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

    // Returns a Firestore Timestamp regardless of whether createdAt is stored as a Timestamp or Long.
    public Timestamp getCreatedAt() {
        if (createdAt instanceof Timestamp) {
            return (Timestamp) createdAt;
        } else if (createdAt instanceof Long) {
            // Convert milliseconds (Long) to Firestore Timestamp
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
}
