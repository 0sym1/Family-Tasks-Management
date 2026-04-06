package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.adapter.TaskAdapter;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TaskListFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private WorkspaceViewModel workspaceViewModel;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private String workspaceId;
    private String workspaceName;
    private TextView tvWorkspaceName;
    private EditText etSearchTask;
    private LinearLayout llFilter;
    private View btnInvite;
    private ImageButton fabAddTask;
    private List<Task> allTasks = new ArrayList<>();
    private Set<String> selectedStatuses = new HashSet<>();
    private NotificationRepository notificationRepository;
    private Workspace currentWorkspace;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        notificationRepository = new NotificationRepository();

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

        btnInvite = view.findViewById(R.id.btnInvite);
        btnInvite.setOnClickListener(v -> showInviteDialog());

        // Navigation to History
        view.findViewById(R.id.btnHistory).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_history, bundle);
        });

        // Navigation to Ranking
        view.findViewById(R.id.btnRanking).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_ranking, bundle);
        });

        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter();
        adapter.setOnTaskClickListener(task -> {
            Bundle bundle = new Bundle();
            bundle.putString("taskId", task.getTaskId());
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(view).navigate(R.id.action_taskList_to_taskDetail, bundle);
        });
        
        adapter.setOnTaskMoreActionsListener(task -> {
            new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                    .setTitle(R.string.delete_task_title)
                    .setMessage(R.string.delete_task_confirm)
                    .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                        taskViewModel.deleteTask(workspaceId, task.getTaskId());
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });
        
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        fabAddTask = view.findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_createTask, bundle);
        });

        view.findViewById(R.id.btnMemberList).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            Navigation.findNavController(v).navigate(R.id.action_taskList_to_inviteMember, bundle);
        });

        observeViewModel();
        if (workspaceId != null) {
            taskViewModel.loadTasks(workspaceId);
            workspaceViewModel.loadWorkspace(workspaceId);
        }
    }

    private void showInviteDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_invite_member, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        EditText etInviteEmail = dialogView.findViewById(R.id.etInviteEmail);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelInvite);
        Button btnSend = dialogView.findViewById(R.id.btnSendInvite);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = etInviteEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), R.string.enter_email_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            sendInvitation(email, dialog);
        });

        dialog.show();
    }

    private void sendInvitation(String email, AlertDialog dialog) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String targetUserId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        String currentUserId = FirebaseAuth.getInstance().getUid();
                        
                        Notification invitation = new Notification();
                        invitation.setUserId(targetUserId);
                        invitation.setType("invitation");
                        invitation.setMessage(getString(R.string.invitation_msg, workspaceName));
                        invitation.setWorkspaceId(workspaceId);
                        invitation.setFromUserId(currentUserId);
                        invitation.setRead(false);
                        
                        notificationRepository.sendNotification(invitation);
                        Toast.makeText(requireContext(), getString(R.string.invitation_sent_to, email), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), R.string.email_not_registered, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        String currentUserId = FirebaseAuth.getInstance().getUid();
        List<Task> filteredList = new ArrayList<>();

        for (Task task : allTasks) {
            String taskTitle = task.getTitle() == null ? "" : task.getTitle();
            boolean matchesSearch = taskTitle.toLowerCase().contains(query);
            boolean matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.contains(normalizeStatus(task.getStatus()));
            
            boolean isOwner = currentWorkspace != null && currentUserId != null && currentUserId.equals(currentWorkspace.getOwnerId());
            boolean isAssigned = currentUserId != null && task.getAssignedToIds() != null && task.getAssignedToIds().contains(currentUserId);
            
            if (matchesSearch && matchesStatus && (isOwner || isAssigned)) {
                filteredList.add(task);
            }
        }

        filteredList.sort(this::compareTasks);
        adapter.setTaskList(filteredList);
    }

    private int compareTasks(Task first, Task second) {
        int byStatus = Integer.compare(getStatusRank(first), getStatusRank(second));
        if (byStatus != 0) return byStatus;

        int byDeadline = compareNullableLongAsc(getTimestampMillis(first.getEndDate()), getTimestampMillis(second.getEndDate()));
        if (byDeadline != 0) return byDeadline;

        int byCreatedAt = compareNullableLongDesc(getTimestampMillis(first.getCreatedAt()), getTimestampMillis(second.getCreatedAt()));
        if (byCreatedAt != 0) return byCreatedAt;

        String firstTitle = first.getTitle() == null ? "" : first.getTitle();
        String secondTitle = second.getTitle() == null ? "" : second.getTitle();
        int byTitle = firstTitle.compareToIgnoreCase(secondTitle);
        if (byTitle != 0) return byTitle;

        String firstTaskId = first.getTaskId() == null ? "" : first.getTaskId();
        String secondTaskId = second.getTaskId() == null ? "" : second.getTaskId();
        return firstTaskId.compareTo(secondTaskId);
    }

    private int getStatusRank(Task task) {
        String normalizedStatus = normalizeStatus(task.getStatus());
        if ("doing".equals(normalizedStatus)) return 0;
        if (Constants.TASK_STATUS_TODO.equals(normalizedStatus)) return 1;
        if ("review".equals(normalizedStatus)) return 2;
        if (Constants.TASK_STATUS_DONE.equals(normalizedStatus)) return 3;
        if (Constants.TASK_STATUS_OVERDUE.equals(normalizedStatus)) return 4;
        return 5;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null) return "";
        String status = rawStatus.trim().toLowerCase(Locale.ROOT);
        if (Constants.TASK_STATUS_IN_PROGRESS.equals(status) || "doing".equals(status)) return "doing";
        if (Constants.TASK_STATUS_PENDING.equals(status) || "review".equals(status)) return "review";
        return status;
    }

    private Long getTimestampMillis(com.google.firebase.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toDate().getTime();
    }

    private int compareNullableLongAsc(Long first, Long second) {
        if (first == null && second == null) return 0;
        if (first == null) return 1;
        if (second == null) return -1;
        return Long.compare(first, second);
    }

    private int compareNullableLongDesc(Long first, Long second) {
        return -compareNullableLongAsc(first, second);
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
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        
        taskViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), R.string.task_deleted, Toast.LENGTH_SHORT).show();
            }
        });

        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null) {
                this.currentWorkspace = workspace;
                String currentUserId = FirebaseAuth.getInstance().getUid();
                if (currentUserId != null && !currentUserId.equals(workspace.getOwnerId())) {
                    btnInvite.setVisibility(View.GONE);
                    fabAddTask.setVisibility(View.GONE);
                    View divider = getView().findViewById(R.id.dividerInvite);
                    if (divider != null) divider.setVisibility(View.GONE);
                } else {
                    btnInvite.setVisibility(View.VISIBLE);
                    fabAddTask.setVisibility(View.VISIBLE);
                    View divider = getView().findViewById(R.id.dividerInvite);
                    if (divider != null) divider.setVisibility(View.VISIBLE);
                }
                applyFilters();
            }
        });
    }
}
