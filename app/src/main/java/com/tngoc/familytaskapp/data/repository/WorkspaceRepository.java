package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceRepository {

    private final FirebaseFirestore db;

    public WorkspaceRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createWorkspace(Workspace workspace, MutableLiveData<String> workspaceIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .add(workspace)
                .addOnSuccessListener(ref -> workspaceIdLiveData.setValue(ref.getId()))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getWorkspacesForUser(String userId, MutableLiveData<List<Workspace>> workspacesLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .whereArrayContains("memberIds", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Workspace> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Workspace.class));
                    }
                    workspacesLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getWorkspace(String workspaceId, MutableLiveData<Workspace> workspaceLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .get()
                .addOnSuccessListener(snapshot -> workspaceLiveData.setValue(snapshot.toObject(Workspace.class)))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateWorkspace(Workspace workspace, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspace.getWorkspaceId())
                .set(workspace)
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteWorkspace(String workspaceId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .delete()
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

