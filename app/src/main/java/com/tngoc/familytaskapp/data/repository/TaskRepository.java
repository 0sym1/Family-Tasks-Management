package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db;

    public TaskRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createTask(String workspaceId, Task task, MutableLiveData<String> taskIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .whereEqualTo("title", task.getTitle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        errorLiveData.setValue("Tên nhiệm vụ này đã tồn tại trong không gian làm việc này.");
                    } else {
                        db.collection(Constants.COLLECTION_WORKSPACES)
                                .document(workspaceId)
                                .collection(Constants.COLLECTION_TASKS)
                                .add(task)
                                .addOnSuccessListener(ref -> {
                                    taskIdLiveData.setValue(ref.getId());
                                    updateWorkspaceTaskCounts(workspaceId);
                                })
                                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getTasksInWorkspace(String workspaceId, MutableLiveData<List<Task>> tasksLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<Task> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Task task = doc.toObject(Task.class);
                            task.setTaskId(doc.getId());
                            list.add(task);
                        }
                        tasksLiveData.setValue(list);
                    }
                });
    }

    public void getTask(String workspaceId, String taskId, MutableLiveData<Task> taskLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        errorLiveData.setValue(e.getMessage());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Task task = snapshot.toObject(Task.class);
                        if (task != null) {
                            task.setTaskId(snapshot.getId());
                            taskLiveData.setValue(task);
                        }
                    }
                });
    }

    public void updateTask(String workspaceId, Task task, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(unused -> {
                    successLiveData.setValue(true);
                    updateWorkspaceTaskCounts(workspaceId);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    successLiveData.setValue(true);
                    updateWorkspaceTaskCounts(workspaceId);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteTask(String workspaceId, String taskId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(unused -> {
                    successLiveData.setValue(true);
                    updateWorkspaceTaskCounts(workspaceId);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    private void updateWorkspaceTaskCounts(String workspaceId) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int total = snapshots.size();
                    int completed = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (Constants.TASK_STATUS_DONE.equals(doc.getString("status"))) {
                            completed++;
                        }
                    }
                    db.collection(Constants.COLLECTION_WORKSPACES)
                            .document(workspaceId)
                            .update("totalTasks", total, "completedTasks", completed);
                });
    }
}
