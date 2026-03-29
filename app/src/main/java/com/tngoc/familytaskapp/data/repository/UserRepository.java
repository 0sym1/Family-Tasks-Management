package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

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
                                user.setUserId(snapshot.getId());
                                if ((user.getDisplayName() == null || user.getDisplayName().isEmpty()) && snapshot.contains("name")) {
                                    user.setDisplayName((String) snapshot.get("name"));
                                }
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

    public void getUsers(List<String> userIds, MutableLiveData<List<User>> usersLiveData, MutableLiveData<String> errorLiveData) {
        if (userIds == null || userIds.isEmpty()) {
            usersLiveData.setValue(new ArrayList<>());
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : userIds) {
            tasks.add(db.collection(Constants.COLLECTION_USERS).document(id).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            List<User> users = new ArrayList<>();
            for (Object res : results) {
                DocumentSnapshot snapshot = (DocumentSnapshot) res;
                if (snapshot.exists()) {
                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        user.setUserId(snapshot.getId());
                        users.add(user);
                    }
                }
            }
            usersLiveData.setValue(users);
        }).addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getUserName(String userId, MutableLiveData<String> nameLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("displayName");
                        if (name == null || name.isEmpty()) {
                            name = snapshot.getString("name");
                        }
                        nameLiveData.setValue(name);
                    } else {
                        nameLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> nameLiveData.setValue(null));
    }

    public void updateUser(User user, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(unused -> successLiveData.setValue(true))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
