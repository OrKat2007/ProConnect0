package com.example.proconnect.model;

import com.google.firebase.Timestamp;

public class userModel {
    private String username;
    private Timestamp createdTimestamp;

    public userModel() {
    }

    public userModel(String username, Timestamp createdTimestamp) {
        this.username = username;
        this.createdTimestamp = createdTimestamp;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
