package com.tngoc.familytaskapp.ui.workspace;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.WorkspaceRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.List;

public class WorkspaceViewModel extends ViewModel {

    private final WorkspaceRepository workspaceRepository;
    private final NotificationRepository notificationRepository;

    public final MutableLiveData<List<Workspace>> workspacesLiveData  = new MutableLiveData<>();
    public final MutableLiveData<Workspace>       workspaceLiveData   = new MutableLiveData<>();
    public final MutableLiveData<String>          workspaceIdLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean>         successLiveData     = new MutableLiveData<>();
    public final MutableLiveData<String>          errorLiveData       = new MutableLiveData<>();

    public WorkspaceViewModel() {
        this.workspaceRepository = new WorkspaceRepository();
        this.notificationRepository = new NotificationRepository();
    }

    public void loadWorkspacesForUser(String userId) {
        workspaceRepository.getWorkspacesForUser(userId, workspacesLiveData, errorLiveData);
    }

    public void loadWorkspace(String workspaceId) {
        workspaceRepository.getWorkspace(workspaceId, workspaceLiveData, errorLiveData);
    }

    public void createWorkspace(Workspace workspace) {
        workspaceRepository.createWorkspace(workspace, workspaceIdLiveData, errorLiveData);
        // Gửi thông báo test khi tạo mới workspace
        if (workspace.getOwnerId() != null) {
            Notification notif = new Notification(
                    workspace.getOwnerId(),
                    "Tạo Workspace thành công",
                    "Bạn vừa tạo mới không gian làm việc: " + workspace.getName(),
                    "test",
                    null
            );
            notificationRepository.sendNotification(notif);
        }
    }

    public void updateWorkspace(Workspace workspace) {
        workspaceRepository.updateWorkspace(workspace, successLiveData, errorLiveData);
    }

    public void deleteWorkspace(String workspaceId) {
        workspaceRepository.deleteWorkspace(workspaceId, successLiveData, errorLiveData);
    }
}
