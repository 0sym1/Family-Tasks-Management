package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceRepository {

    private final FirebaseFirestore db;
    private final Map<String, ListenerRegistration> activeListeners = new HashMap<>();

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
        // Clear old listeners if any
        for (ListenerRegistration listener : activeListeners.values()) {
            listener.remove();
        }
        activeListeners.clear();

        db.collection(Constants.COLLECTION_WORKSPACES)
                .whereArrayContains("memberIds", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    List<Workspace> list = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        workspacesLiveData.setValue(list);
                        return;
                    }

                    final int totalWorkspaces = snapshots.size();
                    final Map<String, Workspace> workspaceMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Workspace workspace = doc.toObject(Workspace.class);
                        workspace.setWorkspaceId(doc.getId());
                        workspaceMap.put(doc.getId(), workspace);

                        // Real-time listener for tasks in each workspace
                        ListenerRegistration taskListener = db.collection(Constants.COLLECTION_WORKSPACES)
                                .document(doc.getId())
                                .collection(Constants.COLLECTION_TASKS)
                                .addSnapshotListener((taskSnapshots, taskError) -> {
                                    if (taskSnapshots != null) {
                                        int total = taskSnapshots.size();
                                        int completed = 0;
                                        for (QueryDocumentSnapshot taskDoc : taskSnapshots) {
                                            if (Constants.TASK_STATUS_DONE.equals(taskDoc.getString("status"))) {
                                                completed++;
                                            }
                                        }
                                        workspace.setTotalTasks(total);
                                        workspace.setCompletedTasks(completed);

                                        // Update the live data whenever a task change occurs
                                        workspacesLiveData.setValue(new ArrayList<>(workspaceMap.values()));
                                    }
                                });
                        
                        activeListeners.put(doc.getId(), taskListener);
                    }
                });
    }

    public void getWorkspace(String workspaceId, MutableLiveData<Workspace> workspaceLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Workspace workspace = snapshot.toObject(Workspace.class);
                        if (workspace != null) {
                            workspace.setWorkspaceId(snapshot.getId());
                            db.collection(Constants.COLLECTION_WORKSPACES)
                                    .document(workspaceId)
                                    .collection(Constants.COLLECTION_TASKS)
                                    .get()
                                    .addOnSuccessListener(taskSnapshots -> {
                                        int total = taskSnapshots.size();
                                        int completed = 0;
                                        for (QueryDocumentSnapshot taskDoc : taskSnapshots) {
                                            if (Constants.TASK_STATUS_DONE.equals(taskDoc.getString("status"))) completed++;
                                        }
                                        workspace.setTotalTasks(total);
                                        workspace.setCompletedTasks(completed);
                                        workspaceLiveData.setValue(workspace);
                                    });
                        }
                    }
                });
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
