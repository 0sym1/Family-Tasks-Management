package com.tngoc.familytaskapp.ui.notification;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final FirebaseFirestore db;

    public final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>             errorLiveData         = new MutableLiveData<>();

    public NotificationViewModel() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadNotifications(String userId) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Notification.class));
                    }
                    notificationsLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void markAsRead(String userId, String notificationId) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true);
    }
}

