package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MyTasksFragment extends Fragment {

    private RecyclerView rvMyTasks, rvCalendar;
    private MyTaskAdapter taskAdapter;
    private CalendarAdapter calendarAdapter;
    private TextView tvMonthYear;
    private Button btnWorkReport;
    
    private Map<String, Task> myTasksMap = new HashMap<>();
    private Map<String, String> userNameCache = new HashMap<>();
    private Set<Date> selectedDates = new HashSet<>();
    
    // Sử dụng Locale.getDefault() để tự động định dạng theo ngôn ngữ đã chọn
    private SimpleDateFormat getMonthYearFormat() {
        return new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyTasks = view.findViewById(R.id.rvMyTasks);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnWorkReport = view.findViewById(R.id.btnWorkReport);

        selectedDates.add(new Date());
        setupCalendar();
        setupTaskList();
        listenToMyTasks();

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        view.findViewById(R.id.btnPrevWeek).setOnClickListener(v -> calendarAdapter.prevWeek());
        view.findViewById(R.id.btnNextWeek).setOnClickListener(v -> calendarAdapter.nextWeek());

        btnWorkReport.setOnClickListener(v -> {
            NavController navController = null;
            try {
                navController = Navigation.findNavController(v);
            } catch (Exception ignored) {}

            if (navController != null) {
                navController.navigate(R.id.action_myTasks_to_workReport);
            } else {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WorkReportFragment())
                        .addToBackStack("myTasksToWorkReport")
                        .commit();
            }
        });

        updateMonthYearDisplay();
    }
    
    private void updateMonthYearDisplay() {
        if (!selectedDates.isEmpty()) {
            Date date = selectedDates.iterator().next();
            String formatted = getMonthYearFormat().format(date);
            // Viết hoa chữ cái đầu cho đẹp
            if (formatted.length() > 0) {
                formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            }
            tvMonthYear.setText(formatted);
        }
    }

    private void setupCalendar() {
        rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        calendarAdapter = new CalendarAdapter(new CalendarAdapter.OnDatesSelectedListener() {
            @Override
            public void onDatesSelected(Set<Date> dates) {
                selectedDates = dates;
                updateMonthYearDisplay();
                applyFilters();
            }

            @Override
            public void onMonthYearChanged(int month, int year) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.YEAR, year);
                String formatted = getMonthYearFormat().format(cal.getTime());
                if (formatted.length() > 0) {
                    formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
                }
                tvMonthYear.setText(formatted);
            }
        });
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void setupTaskList() {
        rvMyTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new MyTaskAdapter();
        
        taskAdapter.setOnTaskClickListener((task, v) -> {
            if (task.getTaskId() == null || task.getWorkspaceId() == null) {
                Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            NavController navController = null;
            try {
                navController = Navigation.findNavController(v);
            } catch (Exception ignored) {}

            if (navController != null) {
                Bundle bundle = new Bundle();
                bundle.putString("taskId", task.getTaskId());
                bundle.putString("workspaceId", task.getWorkspaceId());
                navController.navigate(R.id.action_myTasks_to_taskDetail, bundle);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("taskId", task.getTaskId());
                bundle.putString("workspaceId", task.getWorkspaceId());

                TaskDetailFragment taskDetailFragment = new TaskDetailFragment();
                taskDetailFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, taskDetailFragment)
                        .addToBackStack("myTasksToTaskDetail")
                        .commit();
            }
        });
        
        rvMyTasks.setAdapter(taskAdapter);
    }

    private void listenToMyTasks() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        FirebaseFirestore.getInstance().collectionGroup(Constants.COLLECTION_TASKS)
                .whereArrayContains("assignedToIds", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        myTasksMap.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Task task = doc.toObject(Task.class);
                            task.setTaskId(doc.getId());
                            if (task.getWorkspaceId() == null || task.getWorkspaceId().isEmpty()) {
                                try {
                                    if (doc.getReference().getParent().getParent() != null) {
                                        task.setWorkspaceId(doc.getReference().getParent().getParent().getId());
                                    }
                                } catch (Exception ignored) {}
                            }
                            if (task.getWorkspaceId() != null) {
                                myTasksMap.put(task.getTaskId(), task);
                                fetchOwnerName(task);
                            }
                        }
                        applyFilters();
                    }
                });
    }

    private void fetchOwnerName(Task task) {
        String creatorId = task.getCreatedBy();
        if (creatorId == null) return;
        if (userNameCache.containsKey(creatorId)) {
            task.setOwnerName(userNameCache.get(creatorId));
            return;
        }
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(creatorId).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("displayName");
                        if (name == null) name = snapshot.getString("name");
                        if (name != null) {
                            userNameCache.put(creatorId, name);
                            task.setOwnerName(name);
                            applyFilters();
                        }
                    }
                });
    }

    private void applyFilters() {
        List<Task> filtered = new ArrayList<>();
        for (Task task : myTasksMap.values()) {
            boolean matchesDate = false;
            Calendar taskCal = null;
            if (task.getStartDate() != null) {
                taskCal = Calendar.getInstance();
                taskCal.setTime(task.getStartDate().toDate());
            } else if (task.getEndDate() != null) {
                taskCal = Calendar.getInstance();
                taskCal.setTime(task.getEndDate().toDate());
            }
            if (taskCal != null) {
                int day = taskCal.get(Calendar.DAY_OF_MONTH);
                int month = taskCal.get(Calendar.MONTH);
                int year = taskCal.get(Calendar.YEAR);
                for (Date selDate : selectedDates) {
                    Calendar selCal = Calendar.getInstance();
                    selCal.setTime(selDate);
                    if (selCal.get(Calendar.DAY_OF_MONTH) == day &&
                        selCal.get(Calendar.MONTH) == month &&
                        selCal.get(Calendar.YEAR) == year) {
                        matchesDate = true;
                        break;
                    }
                }
            }
            if (matchesDate) filtered.add(task);
        }
        taskAdapter.setTasks(filtered);
    }
}
