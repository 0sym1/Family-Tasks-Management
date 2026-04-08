package com.tngoc.familytaskapp.ui.history;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.TaskHistory;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private static final String TAG = "HistoryViewModel";

    private final FirebaseFirestore db;

    public final MutableLiveData<List<Object>> historyLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    private List<TaskHistory> cachedHistories = new ArrayList<>();
    private List<Reward> cachedRewards = new ArrayList<>();
    
    private String currentQuery = "";
    private int currentFilter = 0; // 0: All, 1: Tasks, 2: Points

    private ListenerRegistration historyListener;
    private ListenerRegistration rewardListener;

    public HistoryViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void startListening(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            errorLiveData.setValue("workspaceId không hợp lệ");
            return;
        }

        loadingLiveData.setValue(true);
        Log.d(TAG, "startListening() workspaceId = " + workspaceId);
        stopListening();

        historyListener = db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection("histories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    loadingLiveData.setValue(false);
                    if (error != null) {
                        Log.e(TAG, "history listener error: " + error.getMessage(), error);
                        errorLiveData.setValue(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        cachedHistories = value.toObjects(TaskHistory.class);
                        combineAndEmit();
                    }
                });

        rewardListener = db.collection(Constants.COLLECTION_WORKSPACES)
                .document(workspaceId)
                .collection(Constants.COLLECTION_REWARDS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    loadingLiveData.setValue(false);
                    if (error != null) {
                        Log.e(TAG, "reward listener error: " + error.getMessage(), error);
                        errorLiveData.setValue(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        cachedRewards = value.toObjects(Reward.class);
                        combineAndEmit();
                    }
                });
    }

    public void stopListening() {
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }

        if (rewardListener != null) {
            rewardListener.remove();
            rewardListener = null;
        }
    }

    private void combineAndEmit() {
        List<Object> filtered = new ArrayList<>();
        
        if (currentFilter == 0) {
            filtered.addAll(cachedHistories);
            filtered.addAll(cachedRewards);
        } else if (currentFilter == 1) {
            filtered.addAll(cachedHistories);
        } else if (currentFilter == 2) {
            filtered.addAll(cachedRewards);
        }

        // Apply search query
        if (!currentQuery.isEmpty()) {
            List<Object> searchResult = new ArrayList<>();
            String queryLower = currentQuery.toLowerCase();
            for (Object obj : filtered) {
                if (obj instanceof TaskHistory) {
                    TaskHistory h = (TaskHistory) obj;
                    if (h.getTaskName() != null && h.getTaskName().toLowerCase().contains(queryLower)) {
                        searchResult.add(obj);
                    }
                } else if (obj instanceof Reward) {
                    Reward r = (Reward) obj;
                    if (r.getNote() != null && r.getNote().toLowerCase().contains(queryLower)) {
                        searchResult.add(obj);
                    }
                }
            }
            filtered = searchResult;
        }

        Collections.sort(filtered, (o1, o2) -> {
            Timestamp t1 = getCreatedAt(o1);
            Timestamp t2 = getCreatedAt(o2);

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            return t2.compareTo(t1);
        });

        historyLiveData.setValue(filtered);
    }

    private Timestamp getCreatedAt(Object item) {
        if (item instanceof TaskHistory) {
            return ((TaskHistory) item).getCreatedAt();
        } else if (item instanceof Reward) {
            return ((Reward) item).getCreatedAt();
        }
        return null;
    }

    public void setSearchQuery(String query) {
        this.currentQuery = query;
        combineAndEmit();
    }

    public void filterAll() {
        currentFilter = 0;
        combineAndEmit();
    }

    public void filterTasks() {
        currentFilter = 1;
        combineAndEmit();
    }

    public void filterPoints() {
        currentFilter = 2;
        combineAndEmit();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }

    public void loadAllData(String workspaceId) {
        startListening(workspaceId);
    }
}