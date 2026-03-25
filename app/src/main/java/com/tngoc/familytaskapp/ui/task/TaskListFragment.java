package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.tngoc.familytaskapp.data.model.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskListFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private String workspaceId;
    private String workspaceName;
    private TextView tvWorkspaceName;
    private EditText etSearchTask;
    private LinearLayout llFilter;
    private List<Task> allTasks = new ArrayList<>();
    private Set<String> selectedStatuses = new HashSet<>();

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
        }

        tvWorkspaceName = view.findViewById(R.id.tvWorkspaceName);
        if (workspaceName != null) {
            tvWorkspaceName.setText(workspaceName);
        }

        etSearchTask = view.findViewById(R.id.etSearchTask);
        setupSearch();

        llFilter = view.findViewById(R.id.llFilter);
        llFilter.setOnClickListener(v -> showFilterPopup(v));

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

        observeViewModel();
        if (workspaceId != null) {
            taskViewModel.loadTasks(workspaceId);
        }
    }

    private void setupSearch() {
        etSearchTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showFilterPopup(View anchor) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_filter_dropdown, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);

        CheckBox cbDoing = popupView.findViewById(R.id.cbDoing);
        CheckBox cbDone = popupView.findViewById(R.id.cbDone);
        CheckBox cbTodo = popupView.findViewById(R.id.cbTodo);
        CheckBox cbReview = popupView.findViewById(R.id.cbReview);
        Button btnApply = popupView.findViewById(R.id.btnApplyFilter);

        // Restore previous selection
        cbDoing.setChecked(selectedStatuses.contains("doing"));
        cbDone.setChecked(selectedStatuses.contains("done"));
        cbTodo.setChecked(selectedStatuses.contains("todo"));
        cbReview.setChecked(selectedStatuses.contains("review"));

        btnApply.setOnClickListener(v -> {
            selectedStatuses.clear();
            if (cbDoing.isChecked()) selectedStatuses.add("doing");
            if (cbDone.isChecked()) selectedStatuses.add("done");
            if (cbTodo.isChecked()) selectedStatuses.add("todo");
            if (cbReview.isChecked()) selectedStatuses.add("review");

            applyFilters();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, 0, 10);
    }

    private void applyFilters() {
        String query = etSearchTask.getText().toString().toLowerCase();
        List<Task> filteredList = new ArrayList<>();

        for (Task task : allTasks) {
            boolean matchesSearch = task.getTitle().toLowerCase().contains(query);
            boolean matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.contains(task.getStatus());

            if (matchesSearch && matchesStatus) {
                filteredList.add(task);
            }
        }
        adapter.setTaskList(filteredList);
    }

    private void observeViewModel() {
        taskViewModel.tasksLiveData.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                allTasks = tasks;
                applyFilters();
            }
        });

        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
