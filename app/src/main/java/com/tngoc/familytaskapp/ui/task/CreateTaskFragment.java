package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

public class CreateTaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private EditText etTitle, etDescription, etRewardPoints;
    private Button btnCreate;
    private String workspaceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
        }

        etTitle        = view.findViewById(R.id.etTaskTitle);
        etDescription  = view.findViewById(R.id.etTaskDescription);
        etRewardPoints = view.findViewById(R.id.etRewardPoints);
        btnCreate      = view.findViewById(R.id.btnCreateTask);

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc  = etDescription.getText().toString().trim();
            String pts   = etRewardPoints.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            int rewardPoints = pts.isEmpty() ? 0 : Integer.parseInt(pts);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Task task = new Task(workspaceId, title, desc, uid, uid, Constants.TASK_STATUS_TODO, rewardPoints, null);
            taskViewModel.createTask(workspaceId, task);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        taskViewModel.taskIdLiveData.observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Toast.makeText(requireContext(), getString(R.string.task_created), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

