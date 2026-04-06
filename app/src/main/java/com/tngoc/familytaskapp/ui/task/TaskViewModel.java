package com.tngoc.familytaskapp.ui.task;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    public final MutableLiveData<List<Task>> tasksLiveData  = new MutableLiveData<>();
    public final MutableLiveData<Task>       taskLiveData   = new MutableLiveData<>();
    public final MutableLiveData<String>     taskIdLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean>    successLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>     errorLiveData  = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isRepeating = new MutableLiveData<>(false);
    public final MutableLiveData<String> repeatType = new MutableLiveData<>();
    public final MutableLiveData<List<String>> repeatDays = new MutableLiveData<>();
    public final MutableLiveData<String> repeatEndType = new MutableLiveData<>();
    public final MutableLiveData<Integer> repeatCount = new MutableLiveData<>(0);
    public final MutableLiveData<Timestamp> repeatUntil = new MutableLiveData<>();

    public boolean isEditModeInitialized = false;

    public TaskViewModel() {
        this.taskRepository = new TaskRepository();
        this.notificationRepository = new NotificationRepository();
    }

    public void loadTasks(String workspaceId) {
        taskRepository.getTasksInWorkspace(workspaceId, tasksLiveData, errorLiveData);
    }

    public void loadTask(String workspaceId, String taskId) {
        taskRepository.getTask(workspaceId, taskId, taskLiveData, errorLiveData);
    }

    public interface OnTaskActionListener {
        void onSuccess(String taskId);
        void onFailure(String error);
    }

    public void createTask(String workspaceId, Task task, OnTaskActionListener listener) {
        MutableLiveData<String> tempId = new MutableLiveData<>();
        MutableLiveData<String> tempError = new MutableLiveData<>();
        
        taskRepository.createTask(workspaceId, task, tempId, tempError);
        
        tempId.observeForever(id -> {
            if (id != null) {
                taskIdLiveData.setValue(id);
                if (listener != null) listener.onSuccess(id);
            }
        });
        
        tempError.observeForever(err -> {
            if (err != null) {
                errorLiveData.setValue(err);
                if (listener != null) listener.onFailure(err);
            }
        });
    }

    public void updateTask(String workspaceId, Task task, OnTaskActionListener listener) {
        MutableLiveData<Boolean> tempSuccess = new MutableLiveData<>();
        MutableLiveData<String> tempError = new MutableLiveData<>();
        
        taskRepository.updateTask(workspaceId, task, tempSuccess, tempError);
        
        tempSuccess.observeForever(success -> {
            if (Boolean.TRUE.equals(success)) {
                successLiveData.setValue(true);
                if (listener != null) listener.onSuccess(task.getTaskId());
            }
        });
        
        tempError.observeForever(err -> {
            if (err != null) {
                errorLiveData.setValue(err);
                if (listener != null) listener.onFailure(err);
            }
        });
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus) {
        taskRepository.updateTaskStatus(workspaceId, taskId, newStatus, successLiveData, errorLiveData);
    }

    public void deleteTask(String workspaceId, String taskId) {
        taskRepository.deleteTask(workspaceId, taskId, successLiveData, errorLiveData);
    }
    
    public void resetRepeatSettings() {
        isRepeating.setValue(false);
        repeatType.setValue(null);
        repeatDays.setValue(null);
        repeatEndType.setValue(null);
        repeatCount.setValue(0);
        repeatUntil.setValue(null);
        isEditModeInitialized = false;
        
        taskIdLiveData.setValue(null);
        successLiveData.setValue(null);
        errorLiveData.setValue(null);
    }
}
