package com.example.proconnect;

import com.google.firebase.firestore.FieldValue;

public class ReviewModel {
    private String text;
    private String userName;
    private float rating;
    private Object timestamp;

    public ReviewModel(String text, String userName, float rating) {
        this.text = text;
        this.userName = userName;
        this.rating = rating;
        this.timestamp = FieldValue.serverTimestamp();
    }
    public ReviewModel() {
        this.text = "none";
        this.userName = null;
        this.rating = 0;
        this.timestamp = FieldValue.serverTimestamp();
    }

    // Constructor
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}