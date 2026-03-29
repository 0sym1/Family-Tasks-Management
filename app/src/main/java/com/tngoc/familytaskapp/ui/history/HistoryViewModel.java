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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private static final String TAG = "HistoryViewModel";

    private final FirebaseFirestore db;

    public final MutableLiveData<List<Object>> historyLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private List<TaskHistory> cachedHistories = new ArrayList<>();
    private List<Reward> cachedRewards = new ArrayList<>();

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

        Log.d(TAG, "startListening() workspaceId = " + workspaceId);
        stopListening();

        historyListener = db.collection("workspaces")
                .document(workspaceId)
                .collection("histories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "history listener error: " + error.getMessage(), error);
                        errorLiveData.setValue(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        cachedHistories = value.toObjects(TaskHistory.class);
                        Log.d(TAG, "Histories loaded: " + cachedHistories.size());
                        combineAndEmit();
                    }
                });

        rewardListener = db.collection("workspaces")
                .document(workspaceId)
                .collection("rewards")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "reward listener error: " + error.getMessage(), error);
                        errorLiveData.setValue(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        cachedRewards = value.toObjects(Reward.class);
                        Log.d(TAG, "Rewards loaded: " + cachedRewards.size());
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
        List<Object> combined = new ArrayList<>();
        combined.addAll(cachedHistories);
        combined.addAll(cachedRewards);

        Collections.sort(combined, (o1, o2) -> {
            Timestamp t1 = getCreatedAt(o1);
            Timestamp t2 = getCreatedAt(o2);

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            return t2.compareTo(t1);
        });

        Log.d(TAG, "Combined list size: " + combined.size());
        historyLiveData.setValue(combined);
    }

    private Timestamp getCreatedAt(Object item) {
        if (item instanceof TaskHistory) {
            return ((TaskHistory) item).getCreatedAt();
        } else if (item instanceof Reward) {
            return ((Reward) item).getCreatedAt();
        }
        return null;
    }

    public void filterAll() {
        Log.d(TAG, "filterAll()");
        combineAndEmit();
    }

    public void filterTasks() {
        Log.d(TAG, "filterTasks(), size = " + cachedHistories.size());
        historyLiveData.setValue(new ArrayList<>(cachedHistories));
    }

    public void filterPoints() {
        Log.d(TAG, "filterPoints(), size = " + cachedRewards.size());
        historyLiveData.setValue(new ArrayList<>(cachedRewards));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }

    public void createTestData(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            errorLiveData.setValue("workspaceId không hợp lệ");
            return;
        }

        Calendar cal = Calendar.getInstance();

        Reward r1 = new Reward();
        r1.setUserId("user_1");
        r1.setPoints(10);
        r1.setType("task_completed");
        r1.setTaskId("task_A");
        r1.setNote("Hoàn thành nhiệm vụ 'Quét nhà'");
        r1.setCreatedAt(new Timestamp(cal.getTime()));

        cal.add(Calendar.HOUR, -2);

        TaskHistory h1 = new TaskHistory();
        h1.setUserId("user_1");
        h1.setUserName("Phát");
        h1.setTaskId("task_A");
        h1.setTaskName("Quét nhà");
        h1.setAction("task_status_updated");
        h1.setOldValue("Đang làm");
        h1.setNewValue("Hoàn thành");
        h1.setPoint(10);
        h1.setCreatedAt(new Timestamp(cal.getTime()));

        db.collection("workspaces")
                .document(workspaceId)
                .collection("rewards")
                .add(r1)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "createTestData() reward added: " + documentReference.getId()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createTestData() reward failed: " + e.getMessage(), e);
                    errorLiveData.setValue(e.getMessage());
                });

        db.collection("workspaces")
                .document(workspaceId)
                .collection("histories")
                .add(h1)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "createTestData() history added: " + documentReference.getId()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createTestData() history failed: " + e.getMessage(), e);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void loadAllData(String workspaceId) {
        startListening(workspaceId);
    }
}