package com.tngoc.familytaskapp.ui.history;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private final FirebaseFirestore db;

    public final MutableLiveData<List<Task>> historyLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>     errorLiveData   = new MutableLiveData<>();

    public HistoryViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadCompletedTasks(String workspaceId) {
        db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_TASKS)
                .whereEqualTo("status", Constants.TASK_STATUS_DONE)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Task.class));
                    }
                    historyLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

