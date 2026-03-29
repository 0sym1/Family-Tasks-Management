package com.tngoc.familytaskapp.ui.notification;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.WorkspaceRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;
    private final WorkspaceRepository workspaceRepository;

    public final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>             errorLiveData         = new MutableLiveData<>();
    public final MutableLiveData<Boolean>            acceptSuccessLiveData = new MutableLiveData<>();

    public NotificationViewModel() {
        this.notificationRepository = new NotificationRepository();
        this.workspaceRepository = new WorkspaceRepository();
    }

    public void loadNotifications(String userId) {
        notificationRepository.getNotificationsRealtime(userId, notificationsLiveData);
    }

    public void markAsRead(String userId, String notificationId) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true);
    }

    public void acceptInvitation(String userId, Notification notification) {
        workspaceRepository.addMemberToWorkspace(notification.getWorkspaceId(), userId, acceptSuccessLiveData, errorLiveData);
        updateNotificationType(userId, notification.getNotificationId(), "invitation_accepted");
    }

    public void declineInvitation(String userId, String notificationId) {
        updateNotificationType(userId, notificationId, "invitation_declined");
    }

    private void updateNotificationType(String userId, String notificationId, String newType) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("type", newType, "isRead", true);
    }
}
