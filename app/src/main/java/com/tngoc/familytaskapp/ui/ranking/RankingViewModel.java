package com.tngoc.familytaskapp.ui.ranking;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingViewModel extends ViewModel {

    private final FirebaseFirestore db;
    public final MutableLiveData<List<User>> rankingLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public RankingViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadRanking(String workspaceId) {
        if (workspaceId == null || workspaceId.isEmpty()) {
            errorLiveData.setValue("Workspace ID không hợp lệ");
            return;
        }

        // 1. Lấy tất cả rewards trong workspace
        db.collection("workspaces").document(workspaceId).collection("rewards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reward> rewards = queryDocumentSnapshots.toObjects(Reward.class);
                    calculatePoints(workspaceId, rewards);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    private void calculatePoints(String workspaceId, List<Reward> rewards) {
        // Map để cộng dồn điểm theo userId
        Map<String, Integer> userPointsMap = new HashMap<>();
        for (Reward reward : rewards) {
            String uid = reward.getUserId();
            int currentPoints = userPointsMap.getOrDefault(uid, 0);
            userPointsMap.put(uid, currentPoints + reward.getPoints());
        }

        // 2. Lấy thông tin chi tiết từng user (tên, avatar) để hiển thị
        List<User> rankingList = new ArrayList<>();
        if (userPointsMap.isEmpty()) {
            rankingLiveData.setValue(rankingList);
            return;
        }

        db.collection("users")
                .whereIn("userId", new ArrayList<>(userPointsMap.keySet()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setPoints(userPointsMap.getOrDefault(user.getUserId(), 0));
                            rankingList.add(user);
                        }
                    }

                    // 3. Sắp xếp theo điểm giảm dần
                    Collections.sort(rankingList, (u1, u2) -> Integer.compare(u2.getPoints(), u1.getPoints()));
                    rankingLiveData.setValue(rankingList);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
