package com.example.proconnect;

public class usermodel {
    private String uid;
    private String name;
    private String email;
    private String profileImage; // Base64 encoded string
    private boolean isProfessional = false;
    private String profession;
    private String location;
    private float rating = 0.0f;
    private int ratingCount = 0;
    private int ratingSum = 0;// 🆕 Ad   ded profession field

    // Empty constructor required for Firestore
    public usermodel() {}

    // Constructor with parameters
    public usermodel(String uid, String name, String email, boolean isProfessional, String profession, String location) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.isProfessional = isProfessional;
        this.profession = profession;
        this.location = location;
    }

    public usermodel(String uid, String name, String email, boolean isProfessional, String profession, String location, int ratingSum, int ratingCount) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.isProfessional = isProfessional;
        this.profession = profession;
        this.location = location;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
        this.rating = (ratingCount > 0) ? ((float) ratingSum / ratingCount) : 0.0f;
    }


    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isProfessional() {
        return isProfessional;
    }

    public void setProfessional(boolean professional) {
        isProfessional = professional;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(int ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public float getRating() {
        return (ratingCount > 0) ? ((float) ratingSum / ratingCount) : 0.0f;
    }
}
