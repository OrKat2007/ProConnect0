package com.example.proconnect;

public class usermodel {
    private String uid;
    private String name;
    private String email;
    private String profileImage; // Base64 encoded string
    private boolean isProfessional = false;

    // Empty constructor required for Firestore
    public usermodel() {}

    // Constructor with parameters
    public usermodel(String uid, String name, String email, boolean isProfessional) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.isProfessional = isProfessional;
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
}
