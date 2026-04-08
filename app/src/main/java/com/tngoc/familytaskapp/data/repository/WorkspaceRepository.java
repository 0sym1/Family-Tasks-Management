package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WorkspaceRepository {

    private final FirebaseFirestore db;
    private final Map<String, ListenerRegistration> activeListeners = new HashMap<>();

    public WorkspaceRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createWorkspace(Workspace workspace, Consumer<String> onSuccess, Consumer<String> onFailure) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .add(workspace)
                .addOnSuccessListener(ref -> {
                    if (onSuccess != null) onSuccess.accept(ref.getId());
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.accept(e.getMessage());
                });
    }

    public void createWorkspace(Workspace workspace, MutableLiveData<String> workspaceIdLiveData, MutableLiveData<String> errorLiveData) {
        createWorkspace(workspace, 
                id -> { if (workspaceIdLiveData != null) workspaceIdLiveData.setValue(id); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }

    public void addMemberToWorkspace(String workspaceId, String userId, Consumer<Boolean> onSuccess, Consumer<String> onFailure) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .update("memberIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.accept(true);
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.accept(e.getMessage());
                });
    }

    public void addMemberToWorkspace(String workspaceId, String userId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        addMemberToWorkspace(workspaceId, userId,
                success -> { if (successLiveData != null) successLiveData.setValue(success); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }

    public void getWorkspacesForUser(String userId, Consumer<List<Workspace>> callback, Consumer<String> onError) {
        // Clear old listeners if any
        for (ListenerRegistration listener : activeListeners.values()) {
            listener.remove();
        }
        activeListeners.clear();

        db.collection(Constants.COLLECTION_WORKSPACES)
                .whereArrayContains("memberIds", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        if (onError != null) onError.accept(e.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    List<Workspace> list = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        if (callback != null) callback.accept(list);
                        return;
                    }

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
                                        if (callback != null) callback.accept(new ArrayList<>(workspaceMap.values()));
                                    }
                                });
                        
                        activeListeners.put(doc.getId(), taskListener);
                    }
                });
    }

    public void getWorkspacesForUser(String userId, MutableLiveData<List<Workspace>> workspacesLiveData, MutableLiveData<String> errorLiveData) {
        getWorkspacesForUser(userId,
                list -> { if (workspacesLiveData != null) workspacesLiveData.setValue(list); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }

    public void getWorkspace(String workspaceId, Consumer<Workspace> callback, Consumer<String> onError) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        if (onError != null) onError.accept(e.getMessage());
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
                                        if (callback != null) callback.accept(workspace);
                                    });
                        }
                    }
                });
    }

    public void getWorkspace(String workspaceId, MutableLiveData<Workspace> workspaceLiveData, MutableLiveData<String> errorLiveData) {
        getWorkspace(workspaceId,
                ws -> { if (workspaceLiveData != null) workspaceLiveData.setValue(ws); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }

    public void updateWorkspace(Workspace workspace, Consumer<Boolean> onSuccess, Consumer<String> onFailure) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspace.getWorkspaceId())
                .set(workspace)
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.accept(true);
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.accept(e.getMessage());
                });
    }

    public void updateWorkspace(Workspace workspace, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        updateWorkspace(workspace,
                success -> { if (successLiveData != null) successLiveData.setValue(success); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }

    public void deleteWorkspace(String workspaceId, Consumer<Boolean> onSuccess, Consumer<String> onFailure) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.accept(true);
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.accept(e.getMessage());
                });
    }

    public void deleteWorkspace(String workspaceId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        deleteWorkspace(workspaceId,
                success -> { if (successLiveData != null) successLiveData.setValue(success); },
                err -> { if (errorLiveData != null) errorLiveData.setValue(err); });
    }
}
