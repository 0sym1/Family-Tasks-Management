package com.tngoc.familytaskapp.ui.reward;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Reward;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RewardViewModel extends ViewModel {

    private final FirebaseFirestore db;

    public final MutableLiveData<List<Reward>> rewardsLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>       errorLiveData   = new MutableLiveData<>();

    public RewardViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadRewards(String userId) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_REWARDS)
                .orderBy("earnedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Reward> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Reward.class));
                    }
                    rewardsLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

