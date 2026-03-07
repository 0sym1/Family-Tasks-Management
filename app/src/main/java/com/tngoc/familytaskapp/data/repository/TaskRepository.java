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
                .add(task)
                .addOnSuccessListener(ref -> taskIdLiveData.setValue(ref.getId()))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getTasksInWorkspace(String workspaceId, MutableLiveData<List<Task>> tasksLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Task.class));
                    }
                    tasksLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteTask(String workspaceId, String taskId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

