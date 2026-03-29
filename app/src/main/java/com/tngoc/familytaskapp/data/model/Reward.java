package com.tngoc.familytaskapp.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Reward {
    @DocumentId
    private String rewardId;
    private String userId;
    private int points;
    private String type; // task_completed, bonus, penalty
    private String taskId;
    private String note;
    private Timestamp createdAt;

    public Reward() {}

    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
