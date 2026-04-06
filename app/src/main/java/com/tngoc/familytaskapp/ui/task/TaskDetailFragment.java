package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Notification;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.UserRepository;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;
import com.tngoc.familytaskapp.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskDetailFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private WorkspaceViewModel workspaceViewModel;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    
    private String taskId;
    private String workspaceId;
    private String currentUserId;
    private Task currentTask;
    private boolean isOwner = false;

    private TextView tvTitle, tvDesc, tvStatus, tvCreatorName, tvStartDate, tvEndDate, tvPoints, tvRepeatDays;
    private ImageView ivEdit, ivConfirm, ivCreatorAvatar, ivBack, ivRepeatIcon;
    private LinearLayout llPointsSelector, llAssigneesContainer;
    private TableRow trPoints, trRepeatDays;
    private TextView tvPointsStatic;
    private View btnPlus, btnMinus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
            workspaceId = getArguments().getString("workspaceId");
        }

        if (workspaceId != null && taskId != null) {
            taskViewModel.loadTask(workspaceId, taskId);
            workspaceViewModel.loadWorkspace(workspaceId);
        }

        setupClickListeners();
        observeViewModel();
    }

    private void initViews(View v) {
        tvTitle = v.findViewById(R.id.tvTaskTitle);
        tvDesc = v.findViewById(R.id.tvTaskDescription);
        tvStatus = v.findViewById(R.id.tvTaskStatus);
        tvCreatorName = v.findViewById(R.id.tvCreatorName);
        tvStartDate = v.findViewById(R.id.tvStartDate);
        tvEndDate = v.findViewById(R.id.tvEndDate);
        tvPoints = v.findViewById(R.id.tvRewardPoints);
        tvPointsStatic = v.findViewById(R.id.tvRewardPointsStatic);
        tvRepeatDays = v.findViewById(R.id.tvRepeatDays);
        trPoints = v.findViewById(R.id.trPoints);
        trRepeatDays = v.findViewById(R.id.trRepeatDays);
        
        ivEdit = v.findViewById(R.id.ivEditTask);
        ivConfirm = v.findViewById(R.id.ivConfirmStatus);
        ivCreatorAvatar = v.findViewById(R.id.ivCreatorAvatar);
        ivBack = v.findViewById(R.id.ivBack);
        ivRepeatIcon = v.findViewById(R.id.ivRepeatDetail);
        
        llAssigneesContainer = v.findViewById(R.id.llAssigneesContainer);
        llPointsSelector = v.findViewById(R.id.llPointsSelector);
        btnPlus = v.findViewById(R.id.btnPlusPoint);
        btnMinus = v.findViewById(R.id.btnMinusPoint);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        ivEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            bundle.putString("taskId", taskId);
            Navigation.findNavController(v).navigate(R.id.action_taskDetail_to_editTask, bundle);
        });

        ivConfirm.setOnClickListener(v -> {
            Log.d("TaskDetail", "Confirm clicked. currentTask: " + (currentTask != null));
            if (currentTask == null) {
                Toast.makeText(getContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentStatus = currentTask.getStatus();
            Log.d("TaskDetail", "Current status: " + currentStatus + ", isOwner: " + isOwner);
            
            if (isOwner) {
                // CHỦ PHÒNG DUYỆT
                if (Constants.TASK_STATUS_PENDING.equals(currentStatus) || "review".equals(currentStatus)) {
                    Toast.makeText(getContext(), "Đang duyệt nhiệm vụ...", Toast.LENGTH_SHORT).show();
                    taskViewModel.updateTaskStatus(workspaceId, taskId, Constants.TASK_STATUS_DONE);
                    if (currentTask.getAssignedToIds() != null) {
                        for (String userId : currentTask.getAssignedToIds()) {
                            sendNotification(userId, 
                                    "Nhiệm vụ đã được duyệt", 
                                    "Chủ phòng đã duyệt nhiệm vụ: " + currentTask.getTitle());
                        }
                    }
                } else if (!Constants.TASK_STATUS_DONE.equals(currentStatus)) {
                    Toast.makeText(getContext(), "Đang hoàn thành nhiệm vụ...", Toast.LENGTH_SHORT).show();
                    taskViewModel.updateTaskStatus(workspaceId, taskId, Constants.TASK_STATUS_DONE);
                } else {
                    Toast.makeText(getContext(), "Nhiệm vụ này đã hoàn thành rồi", Toast.LENGTH_SHORT).show();
                }
            } else {
                // THÀNH VIÊN NỘP
                boolean isAssigned = currentTask.getAssignedToIds() != null && currentTask.getAssignedToIds().contains(currentUserId);
                if (isAssigned) {
                    if (Constants.TASK_STATUS_DONE.equals(currentStatus)) {
                        Toast.makeText(getContext(), "Nhiệm vụ đã hoàn thành", Toast.LENGTH_SHORT).show();
                    } else if (Constants.TASK_STATUS_PENDING.equals(currentStatus) || "review".equals(currentStatus)) {
                        Toast.makeText(getContext(), "Đang chờ chủ phòng duyệt", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Đang nộp nhiệm vụ...", Toast.LENGTH_SHORT).show();
                        taskViewModel.updateTaskStatus(workspaceId, taskId, Constants.TASK_STATUS_PENDING);
                        sendNotification(currentTask.getCreatedBy(), 
                                "Yêu cầu duyệt nhiệm vụ", 
                                "Thành viên đã nộp nhiệm vụ: " + currentTask.getTitle());
                    }
                } else {
                    Toast.makeText(getContext(), "Bạn không có quyền nộp nhiệm vụ này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (currentTask != null && !Constants.TASK_STATUS_DONE.equals(currentTask.getStatus())) {
                currentTask.setRewardPoints(currentTask.getRewardPoints() + 1);
                taskViewModel.updateTask(workspaceId, currentTask, null);
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (currentTask != null && currentTask.getRewardPoints() > 0 
                && !Constants.TASK_STATUS_DONE.equals(currentTask.getStatus())) {
                currentTask.setRewardPoints(currentTask.getRewardPoints() - 1);
                taskViewModel.updateTask(workspaceId, currentTask, null);
            }
        });
    }

    private void sendNotification(String receiverId, String title, String message) {
        Notification notif = new Notification(
                receiverId,
                title,
                message,
                Constants.NOTIF_TASK_DONE,
                workspaceId
        );
        notificationRepository.sendNotification(notif);
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null) {
                isOwner = workspace.getOwnerId().equals(currentUserId);
                updateUIForRole();
            }
        });

        taskViewModel.taskLiveData.observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                currentTask = task;
                displayTaskData(task);
                updateUIForRole();
            }
        });

        taskViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                taskViewModel.successLiveData.setValue(null);
            }
        });
        
        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                taskViewModel.errorLiveData.setValue(null);
            }
        });
    }

    private void updateUIForRole() {
        if (currentTask == null) return;
        String status = currentTask.getStatus();
        boolean isDone = Constants.TASK_STATUS_DONE.equals(status);
        boolean isPending = Constants.TASK_STATUS_PENDING.equals(status) || "review".equals(status);

        // Reset visual states
        ivConfirm.setAlpha(1.0f);
        ivConfirm.clearColorFilter();

        if (isOwner) {
            ivEdit.setVisibility(isDone ? View.GONE : View.VISIBLE);
            trPoints.setVisibility(View.VISIBLE);
            
            if (isDone) {
                llPointsSelector.setVisibility(View.GONE);
                tvPointsStatic.setVisibility(View.VISIBLE);
                ivConfirm.setVisibility(View.GONE);
            } else {
                llPointsSelector.setVisibility(View.VISIBLE);
                tvPointsStatic.setVisibility(View.GONE);
                ivConfirm.setVisibility(View.VISIBLE);
                
                if (isPending) {
                    ivConfirm.setColorFilter(android.graphics.Color.parseColor("#FFD700")); // Màu vàng chờ duyệt
                }
            }
        } else {
            ivEdit.setVisibility(View.GONE);
            llPointsSelector.setVisibility(View.GONE);
            tvPointsStatic.setVisibility(View.VISIBLE);
            
            trPoints.setVisibility(isDone ? View.VISIBLE : View.GONE);
            
            boolean isAssigned = currentTask.getAssignedToIds() != null && currentTask.getAssignedToIds().contains(currentUserId);
            if (isAssigned) {
                if (isDone) {
                    ivConfirm.setVisibility(View.GONE);
                } else if (isPending) {
                    ivConfirm.setVisibility(View.VISIBLE);
                    ivConfirm.setColorFilter(android.graphics.Color.parseColor("#888888"));
                    ivConfirm.setAlpha(0.5f);
                } else {
                    ivConfirm.setVisibility(View.VISIBLE);
                }
            } else {
                ivConfirm.setVisibility(View.GONE);
            }
        }
    }

    private void displayTaskData(Task task) {
        tvTitle.setText(task.getTitle());
        tvDesc.setText(task.getDescription());
        tvPoints.setText(String.valueOf(task.getRewardPoints()));
        tvPointsStatic.setText(String.valueOf(task.getRewardPoints()));
        
        boolean isRepeat = task.isRepeat() || task.isRepeating();
        if (ivRepeatIcon != null) {
            ivRepeatIcon.setVisibility(isRepeat ? View.VISIBLE : View.GONE);
        }

        String status = task.getStatus();
        if (Constants.TASK_STATUS_DONE.equalsIgnoreCase(status)) {
            tvStatus.setText(getString(R.string.status_done));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#59D595"));
            tvStatus.getBackground().setTint(android.graphics.Color.parseColor("#1B3022"));
        } else if (Constants.TASK_STATUS_IN_PROGRESS.equalsIgnoreCase(status) || "doing".equalsIgnoreCase(status) || Constants.TASK_STATUS_TODO.equalsIgnoreCase(status)) {
            // "CHƯA LÀM" (TODO) cũng chuyển thành "ĐANG LÀM" (DOING) theo yêu cầu
            tvStatus.setText(getString(R.string.status_doing));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#5EB5F7"));
            tvStatus.getBackground().setTint(android.graphics.Color.parseColor("#1A2B3D"));
        } else if (Constants.TASK_STATUS_PENDING.equalsIgnoreCase(status) || "review".equalsIgnoreCase(status)) {
            tvStatus.setText(getString(R.string.status_review));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FFD700"));
            tvStatus.getBackground().setTint(android.graphics.Color.parseColor("#332D00"));
        } else if (Constants.TASK_STATUS_OVERDUE.equalsIgnoreCase(status)) {
            tvStatus.setText(getString(R.string.status_overdue));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"));
            tvStatus.getBackground().setTint(android.graphics.Color.parseColor("#3D1A1A"));
        }

        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        if (task.getStartDate() != null) {
            tvStartDate.setText(dateOnlySdf.format(task.getStartDate().toDate()));
        }

        String timePart = task.getEndTime() != null ? task.getEndTime().replace(":", "h") : "12h00";
        
        if (isRepeat) {
            String repeatInfo = "";
            if ("Daily".equalsIgnoreCase(task.getRepeatType())) {
                repeatInfo = "Hằng ngày";
                trRepeatDays.setVisibility(View.GONE);
            } else if ("Weekly".equalsIgnoreCase(task.getRepeatType())) {
                repeatInfo = "Hằng tuần";
                trRepeatDays.setVisibility(View.VISIBLE);
                
                List<String> days = task.getRepeatDays();
                if (days != null && !days.isEmpty()) {
                    Map<String, String> dayMap = new HashMap<>();
                    dayMap.put("mon", "Th 2");
                    dayMap.put("tue", "Th 3");
                    dayMap.put("wed", "Th 4");
                    dayMap.put("thu", "Th 5");
                    dayMap.put("fri", "Th 6");
                    dayMap.put("sat", "Th 7");
                    dayMap.put("sun", "CN");
                    
                    List<String> formattedDays = new java.util.ArrayList<>();
                    for (String day : days) {
                        if (dayMap.containsKey(day.toLowerCase())) {
                            formattedDays.add(dayMap.get(day.toLowerCase()));
                        }
                    }
                    tvRepeatDays.setText(TextUtils.join(", ", formattedDays));
                } else {
                    tvRepeatDays.setText("Chưa chọn ngày");
                }
            }
            tvEndDate.setText(timePart + " " + repeatInfo);
        } else {
            trRepeatDays.setVisibility(View.GONE);
            if (task.getEndDate() != null) {
                tvEndDate.setText(timePart + " " + dateOnlySdf.format(task.getEndDate().toDate()));
            } else {
                tvEndDate.setText(timePart);
            }
        }

        MutableLiveData<String> creatorNameLiveData = new MutableLiveData<>();
        creatorNameLiveData.observe(getViewLifecycleOwner(), name -> {
            if (name != null) tvCreatorName.setText(name);
        });
        userRepository.getUserName(task.getCreatedBy(), creatorNameLiveData);
        
        llAssigneesContainer.removeAllViews();
        if (task.getAssignedToIds() != null) {
            for (String assigneeId : task.getAssignedToIds()) {
                addAssigneeRow(assigneeId);
            }
        }
    }

    private void addAssigneeRow(String userId) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_assignee_row, llAssigneesContainer, false);
        ImageView ivAvatar = row.findViewById(R.id.ivAssigneeAvatar);
        TextView tvName = row.findViewById(R.id.tvAssigneeName);
        
        MutableLiveData<String> nameLiveData = new MutableLiveData<>();
        nameLiveData.observe(getViewLifecycleOwner(), name -> {
            if (name != null) tvName.setText(name);
        });
        userRepository.getUserName(userId, nameLiveData);
        
        llAssigneesContainer.addView(row);
    }
}
