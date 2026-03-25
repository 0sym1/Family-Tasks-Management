package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getUserName(String userId, MutableLiveData<String> nameLiveData) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameLiveData.setValue(documentSnapshot.getString("name"));
                    }
                });
    }
}
