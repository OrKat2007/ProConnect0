package com.example.proconnect;

public class ReviewModel {
    private String reviewerEmail;  // Email of the reviewer
    private String text;           // Review text
    private float rating;          // Individual rating
    private long timestamp;        // Timestamp of review

    // Empty constructor needed for Firestore
    public ReviewModel() {}

    // Constructor with all fields
    public ReviewModel(String reviewerEmail, String text, float rating, long timestamp) {
        this.reviewerEmail = reviewerEmail;
        this.text = text;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
