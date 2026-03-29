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
    private String workspaceId;
    private String fromUserId;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
        this.isRead = false;
        this.createdAt = Timestamp.now();
    }

    public Notification(String userId, String title, String message, String type, String workspaceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.workspaceId = workspaceId;
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

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    @PropertyName("isRead")
    public boolean isRead() { return isRead; }
    @PropertyName("isRead")
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
