package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;

public class Reward {
    @DocumentId
    private String rewardId;
    private String userId;
    private String taskId;
    private String taskTitle;
    private int pointsEarned;
    private Timestamp earnedAt;

    public Reward() {}

    public Reward(String userId, String taskId, String taskTitle, int pointsEarned) {
        this.userId = userId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.pointsEarned = pointsEarned;
        this.earnedAt = Timestamp.now();
    }

    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public Timestamp getEarnedAt() { return earnedAt; }
    public void setEarnedAt(Timestamp earnedAt) { this.earnedAt = earnedAt; }
}

