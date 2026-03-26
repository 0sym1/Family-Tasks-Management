package com.tngoc.familytaskapp.ui.notification;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;

    public final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>             errorLiveData         = new MutableLiveData<>();

    public NotificationViewModel() {
        this.notificationRepository = new NotificationRepository();
    }

    public void loadNotifications(String userId) {
        notificationRepository.getNotificationsRealtime(userId, notificationsLiveData);
    }

    public void markAsRead(String userId, String notificationId) {
        // Cập nhật trạng thái đã đọc cho một thông báo cụ thể nếu cần
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true);
    }
}
