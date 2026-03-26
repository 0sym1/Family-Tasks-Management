package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Notification {
    @DocumentId
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type;
    private String refId;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(String userId, String title, String message, String type, String refId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.refId = refId;
        this.isRead = false;
        this.createdAt = Timestamp.now();
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    @PropertyName("isRead")
    public boolean isRead() { return isRead; }
    @PropertyName("isRead")
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
