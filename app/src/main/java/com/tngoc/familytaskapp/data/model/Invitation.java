package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;

public class Invitation {
    @DocumentId
    private String invitationId;
    private String workspaceId;
    private String workspaceName;
    private String invitedByUserId;
    private String invitedEmail;
    private String status;  // "pending" | "accepted" | "declined"
    private Timestamp createdAt;

    public Invitation() {}

    public Invitation(String workspaceId, String workspaceName, String invitedByUserId, String invitedEmail) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.invitedByUserId = invitedByUserId;
        this.invitedEmail = invitedEmail;
        this.status = "pending";
        this.createdAt = Timestamp.now();
    }

    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }

    public String getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(String invitedByUserId) { this.invitedByUserId = invitedByUserId; }

    public String getInvitedEmail() { return invitedEmail; }
    public void setInvitedEmail(String invitedEmail) { this.invitedEmail = invitedEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

