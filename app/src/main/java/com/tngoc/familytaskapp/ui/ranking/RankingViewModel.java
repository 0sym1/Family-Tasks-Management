package com.tngoc.familytaskapp.ui.ranking;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingViewModel extends ViewModel {

    private final FirebaseFirestore db;
    public final MutableLiveData<List<User>> rankingLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public RankingViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadRanking(String workspaceId) {
        if (workspaceId == null || workspaceId.isEmpty()) {
            errorLiveData.setValue("Workspace ID không hợp lệ");
            return;
        }

        loadingLiveData.setValue(true);

        // 1. Lấy thông tin workspace để biết danh sách thành viên (memberIds)
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Workspace workspace = documentSnapshot.toObject(Workspace.class);
                    if (workspace != null && workspace.getMemberIds() != null && !workspace.getMemberIds().isEmpty()) {
                        loadRewardsAndMembers(workspaceId, workspace.getMemberIds());
                    } else if (documentSnapshot.exists()) {
                        // Fallback: Thử lấy trực tiếp list string nếu model không map được
                        List<String> rawMemberIds = (List<String>) documentSnapshot.get("memberIds");
                        if (rawMemberIds != null && !rawMemberIds.isEmpty()) {
                            loadRewardsAndMembers(workspaceId, rawMemberIds);
                        } else {
                            rankingLiveData.setValue(new ArrayList<>());
                            loadingLiveData.setValue(false);
                        }
                    } else {
                        rankingLiveData.setValue(new ArrayList<>());
                        loadingLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RankingViewModel", "Error loading workspace", e);
                    errorLiveData.setValue(e.getMessage());
                    loadingLiveData.setValue(false);
                });
    }

    private void loadRewardsAndMembers(String workspaceId, List<String> memberIds) {
        // 2. Lấy tất cả rewards trong workspace để tính điểm
        db.collection(Constants.COLLECTION_WORKSPACES).document(workspaceId).collection(Constants.COLLECTION_REWARDS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reward> rewards = queryDocumentSnapshots.toObjects(Reward.class);
                    
                    Map<String, Integer> userPointsMap = new HashMap<>();
                    for (Reward reward : rewards) {
                        String uid = reward.getUserId();
                        if (uid != null) {
                            int currentPoints = userPointsMap.getOrDefault(uid, 0);
                            userPointsMap.put(uid, currentPoints + reward.getPoints());
                        }
                    }

                    fetchMemberDetails(memberIds, userPointsMap);
                })
                .addOnFailureListener(e -> {
                    Log.e("RankingViewModel", "Error loading rewards", e);
                    errorLiveData.setValue(e.getMessage());
                    loadingLiveData.setValue(false);
                });
    }

    private void fetchMemberDetails(List<String> memberIds, Map<String, Integer> userPointsMap) {
        // 3. Lấy thông tin chi tiết của TẤT CẢ thành viên
        List<String> limitedIds = memberIds.size() > 10 ? memberIds.subList(0, 10) : memberIds;

        db.collection(Constants.COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), limitedIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> rankingList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUserId(doc.getId());
                            // Đồng nhất trường tên nếu model không khớp
                            if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
                                String name = doc.getString("displayName");
                                if (name == null) name = doc.getString("name");
                                user.setDisplayName(name != null ? name : "User");
                            }
                            user.setPoints(userPointsMap.getOrDefault(user.getUserId(), 0));
                            rankingList.add(user);
                        }
                    }

                    // 4. Sắp xếp theo điểm giảm dần
                    Collections.sort(rankingList, (u1, u2) -> Integer.compare(u2.getPoints(), u1.getPoints()));
                    rankingLiveData.setValue(rankingList);
                    loadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("RankingViewModel", "Error fetching member details", e);
                    errorLiveData.setValue(e.getMessage());
                    loadingLiveData.setValue(false);
                });
    }
}