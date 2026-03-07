package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;
import java.util.List;

public class Workspace {
    @DocumentId
    private String workspaceId;
    private String name;
    private String description;
    private String ownerId;
    private List<String> memberIds;
    private Timestamp createdAt;

    public Workspace() {}

    public Workspace(String workspaceId, String name, String description, String ownerId, List<String> memberIds) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.memberIds = memberIds;
        this.createdAt = Timestamp.now();
    }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

