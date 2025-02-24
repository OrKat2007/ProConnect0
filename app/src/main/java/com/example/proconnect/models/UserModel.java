package com.example.proconnect.models;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String profileImage; // Base64 encoded string
    private boolean isProfessional = false;
    private String profession;
    private String location;
    private int age;
    private String languages;
    private String availability;

    // Empty constructor required for Firestore
    public UserModel() {}

    // Constructor with parameters
    public UserModel(String uid, String name, String email, boolean isProfessional, String profession, String location) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.isProfessional = isProfessional;
        this.profession = profession;
        this.location = location;
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

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}
