package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;

public class User {
    @DocumentId
    private String userId;
    private String displayName;
    private String email;
    private String avatarUrl;
    private int points;

    public User() {}

    public User(String userId, String displayName, String email, String avatarUrl, int points) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.points = points;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}

