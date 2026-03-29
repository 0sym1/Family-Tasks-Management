package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void sendNotification(Notification notification) {
        db.collection(Constants.COLLECTION_USERS)
                .document(notification.getUserId())
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .add(notification);
    }

    public ListenerRegistration getNotificationsRealtime(String userId, MutableLiveData<List<Notification>> notificationsLiveData) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        List<Notification> list = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                n.setNotificationId(doc.getId());
                                list.add(n);
                            }
                        }
                        list.sort((o1, o2) -> {
                            if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                        });
                        notificationsLiveData.setValue(list);
                    }
                });
    }

    public void deleteNotification(String userId, String notificationId, MutableLiveData<Boolean> successLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (successLiveData != null) successLiveData.setValue(true);
                });
    }

    public void getUnreadCountRealtime(String userId, MutableLiveData<Integer> unreadCountLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        unreadCountLiveData.setValue(snapshots.size());
                    }
                });
    }

    public void markAllAsRead(String userId) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) return;
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit();
                });
    }
}
