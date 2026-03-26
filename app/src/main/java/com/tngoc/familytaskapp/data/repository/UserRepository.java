package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.utils.Constants;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createUser(User user, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getUser(String userId, MutableLiveData<User> userLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        try {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                // Set userId từ DocumentId
                                user.setUserId(snapshot.getId());
                                // Nếu displayName null, thử lấy từ "name"
                                if ((user.getDisplayName() == null || user.getDisplayName().isEmpty()) && snapshot.contains("name")) {
                                    user.setDisplayName((String) snapshot.get("name"));
                                }
                                // Set default values nếu null
                                if (user.getEmail() == null) user.setEmail("");
                                if (user.getAvatarUrl() == null) user.setAvatarUrl("");
                            }
                            userLiveData.setValue(user);
                        } catch (Exception e) {
                            errorLiveData.setValue("Error parsing user data: " + e.getMessage());
                        }
                    } else {
                        errorLiveData.setValue("User not found");
                    }
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void updateUser(User user, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

