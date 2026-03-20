package com.moodtune.app.models;

public class UserModel {
    public String uid;
    public String name;
    public String email;
    public String currentMood;
    public boolean isFirstTime;
    public long createdAt;

    public UserModel() {} // Empty constructor required for Firebase

    public UserModel(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isFirstTime = true;
        this.createdAt = System.currentTimeMillis();
    }
}

