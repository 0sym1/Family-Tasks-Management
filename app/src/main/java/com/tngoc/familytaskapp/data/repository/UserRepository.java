package com.tngoc.familytaskapp.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
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
                                if (user.getAvatarUrl() == null) {
                                    if (snapshot.contains("avt_url")) {
                                        user.setAvatarUrl(snapshot.getString("avt_url"));
                                    } else {
                                        user.setAvatarUrl("");
                                    }
                                }
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

    public void uploadAvatar(String userId, Uri imageUri, MutableLiveData<String> avatarUrlLiveData, MutableLiveData<String> errorLiveData) {
        if (imageUri == null) {
            errorLiveData.setValue("Lỗi: Ảnh không hợp lệ");
            return;
        }

        try {
            // Sử dụng tên file không có đuôi mở rộng hoặc tự động lấy từ Uri để tránh lỗi format
            StorageReference ref = storage.getReference().child("avatars/" + userId);
            
            // Thêm Metadata để Firebase hiểu đây là ảnh
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            ref.putFile(imageUri, metadata)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("UploadAvatar", "Upload successful, getting download URL");
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            Log.d("UploadAvatar", "Download URL: " + url);
                            
                            // Cập nhật cả "avatarUrl" và "avt_url" để tương thích với model User
                            Map<String, Object> update = new HashMap<>();
                            update.put("avatarUrl", url);
                            update.put("avt_url", url);

                            db.collection(Constants.COLLECTION_USERS).document(userId).update(update)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("UploadAvatar", "Firestore update successful");
                                        avatarUrlLiveData.setValue(url);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UploadAvatar", "Firestore update failed: " + e.getMessage(), e);
                                        errorLiveData.setValue("Lỗi cập nhật Firestore: " + e.getMessage());
                                    });
                        }).addOnFailureListener(e -> {
                            Log.e("UploadAvatar", "Get download URL failed: " + e.getMessage(), e);
                            errorLiveData.setValue("Lỗi lấy link ảnh: " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UploadAvatar", "Upload failed: " + e.getMessage(), e);
                        // Trả về lỗi chi tiết thay vì lỗi chung chung
                        errorLiveData.setValue("Lỗi tải ảnh lên Storage: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e("UploadAvatar", "Exception: " + e.getMessage(), e);
            errorLiveData.setValue("Lỗi hệ thống: " + e.getMessage());
        }
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
