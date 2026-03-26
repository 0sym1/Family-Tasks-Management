package com.tngoc.familytaskapp.ui.task;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    public final MutableLiveData<List<Task>> tasksLiveData  = new MutableLiveData<>();
    public final MutableLiveData<String>     taskIdLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean>    successLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>     errorLiveData  = new MutableLiveData<>();

    public TaskViewModel() {
        this.taskRepository = new TaskRepository();
        this.notificationRepository = new NotificationRepository();
    }

    public void loadTasks(String workspaceId) {
        taskRepository.getTasksInWorkspace(workspaceId, tasksLiveData, errorLiveData);
    }

    public void createTask(String workspaceId, Task task) {
        taskRepository.createTask(workspaceId, task, taskIdLiveData, errorLiveData);
        
        // Gửi thông báo cho người được giao task
        if (task.getAssignedToIds() != null) {
            for (String userId : task.getAssignedToIds()) {
                Notification notif = new Notification(
                        userId,
                        "Bạn có nhiệm vụ mới",
                        "Bạn vừa được giao nhiệm vụ: " + task.getTitle(),
                        "task_assigned",
                        workspaceId
                );
                notificationRepository.sendNotification(notif);
            }
        }

        // Thông báo TEST cho người tạo task
        if (task.getCreatedBy() != null) {
            Notification testNotif = new Notification(
                    task.getCreatedBy(),
                    "Tạo nhiệm vụ thành công",
                    "Bạn vừa tạo mới nhiệm vụ: " + task.getTitle(),
                    "test",
                    workspaceId
            );
            notificationRepository.sendNotification(testNotif);
        }
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus) {
        taskRepository.updateTaskStatus(workspaceId, taskId, newStatus, successLiveData, errorLiveData);
        // Có thể thêm thông báo "task_done" ở đây nếu newStatus == "done"
    }

    public void deleteTask(String workspaceId, String taskId) {
        taskRepository.deleteTask(workspaceId, taskId, successLiveData, errorLiveData);
    }
}
