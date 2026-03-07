package com.tngoc.familytaskapp.ui.task;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository taskRepository;

    public final MutableLiveData<List<Task>> tasksLiveData  = new MutableLiveData<>();
    public final MutableLiveData<String>     taskIdLiveData = new MutableLiveData<>();
    public final MutableLiveData<Boolean>    successLiveData = new MutableLiveData<>();
    public final MutableLiveData<String>     errorLiveData  = new MutableLiveData<>();

    public TaskViewModel() {
        this.taskRepository = new TaskRepository();
    }

    public void loadTasks(String workspaceId) {
        taskRepository.getTasksInWorkspace(workspaceId, tasksLiveData, errorLiveData);
    }

    public void createTask(String workspaceId, Task task) {
        taskRepository.createTask(workspaceId, task, taskIdLiveData, errorLiveData);
    }

    public void updateTaskStatus(String workspaceId, String taskId, String newStatus) {
        taskRepository.updateTaskStatus(workspaceId, taskId, newStatus, successLiveData, errorLiveData);
    }

    public void deleteTask(String workspaceId, String taskId) {
        taskRepository.deleteTask(workspaceId, taskId, successLiveData, errorLiveData);
    }
}

