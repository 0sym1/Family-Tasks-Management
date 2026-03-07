package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tngoc.familytaskapp.R;

public class TaskDetailFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private String taskId;
    private String workspaceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        if (getArguments() != null) {
            taskId      = getArguments().getString("taskId");
            workspaceId = getArguments().getString("workspaceId");
        }

        view.findViewById(R.id.btnMarkDone).setOnClickListener(v -> {
            if (workspaceId != null && taskId != null) {
                taskViewModel.updateTaskStatus(workspaceId, taskId, "done");
            }
        });

        observeViewModel();
    }

    private void observeViewModel() {
        taskViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success))
                Toast.makeText(requireContext(), getString(R.string.task_completed), Toast.LENGTH_SHORT).show();
        });

        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

