package com.tngoc.familytaskapp.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class TaskHistory {
    @DocumentId
    private String historyId;
    private String action;
    private String taskId;
    private String taskName;
    private String userId;
    private String userName;
    private String oldValue;
    private String newValue;
    private int point;
    private Timestamp createdAt;

    public TaskHistory() {}

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
