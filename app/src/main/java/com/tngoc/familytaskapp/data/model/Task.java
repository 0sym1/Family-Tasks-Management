package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;
import java.util.List;

public class Task {
    @DocumentId
    private String taskId;
    private String workspaceId;
    private String title;
    private String description;
    private String assignedTo;   // userId
    private String createdBy;    // userId
    private String status;       // "todo" | "in_progress" | "done"
    private int rewardPoints;
    private Timestamp dueDate;
    private Timestamp createdAt;

    // Repeat settings
    private boolean repeating = false;
    private String repeatType; // "Daily", "Weekly"
    private List<String> repeatDays; // ["Mon", "Tue", ...]
    private String repeatEndType; // "Never", "AfterTimes", "AfterDate"
    private int repeatCount;
    private Timestamp repeatUntil;

    public Task() {}

    public Task(String workspaceId, String title, String description, String assignedTo, String createdBy, String status, int rewardPoints, Timestamp dueDate) {
        this.workspaceId = workspaceId;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.status = status;
        this.rewardPoints = rewardPoints;
        this.dueDate = dueDate;
        this.createdAt = Timestamp.now();
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }

    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isRepeating() { return repeating; }
    public void setRepeating(boolean repeating) { this.repeating = repeating; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public List<String> getRepeatDays() { return repeatDays; }
    public void setRepeatDays(List<String> repeatDays) { this.repeatDays = repeatDays; }

    public String getRepeatEndType() { return repeatEndType; }
    public void setRepeatEndType(String repeatEndType) { this.repeatEndType = repeatEndType; }

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }

    public Timestamp getRepeatUntil() { return repeatUntil; }
    public void setRepeatUntil(Timestamp repeatUntil) { this.repeatUntil = repeatUntil; }
}
