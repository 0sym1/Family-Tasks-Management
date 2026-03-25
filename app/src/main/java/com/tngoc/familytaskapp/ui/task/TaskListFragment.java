package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.adapter.TaskAdapter;

public class TaskListFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private String workspaceId;
    private String workspaceName;
    private TextView tvWorkspaceName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
            workspaceName = getArguments().getString("workspaceName");
            Log.d("TaskListFragment", "Workspace ID: " + workspaceId);
        }

        tvWorkspaceName = view.findViewById(R.id.tvWorkspaceName);
        if (workspaceName != null) {
            tvWorkspaceName.setText(workspaceName);
        }

        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter();
        adapter.setOnTaskClickListener(task -> {
            Bundle bundle = new Bundle();
            bundle.putString("taskId", task.getTaskId());
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(view).navigate(R.id.action_taskList_to_taskDetail, bundle);
        });
        recyclerView.setAdapter(adapter);

        // Update back button to finish activity and return to HomeActivity
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        view.findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_createTask, bundle);
        });

        view.findViewById(R.id.btnInvite).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_inviteMember, bundle);
        });

        observeViewModel();
        if (workspaceId != null) {
            taskViewModel.loadTasks(workspaceId);
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy mã workspace", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        taskViewModel.tasksLiveData.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                Log.d("TaskListFragment", "Tasks loaded: " + tasks.size());
                adapter.setTaskList(tasks);
            }
        });

        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e("TaskListFragment", "Error: " + error);
                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
