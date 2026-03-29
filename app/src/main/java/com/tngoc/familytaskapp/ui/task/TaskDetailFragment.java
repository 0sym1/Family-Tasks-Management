package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
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
import java.util.Locale;

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

    private TextView tvTitle, tvDesc, tvStatus, tvCreatorName, tvAssigneeName, tvStartDate, tvEndDate, tvPoints;
    private ImageView ivEdit, ivConfirm, ivCreatorAvatar, ivAssigneeAvatar, ivBack;
    private LinearLayout llPointsSelector;
    private TableRow trPoints;
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
        
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
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
        tvAssigneeName = v.findViewById(R.id.tvAssigneeName);
        tvStartDate = v.findViewById(R.id.tvStartDate);
        tvEndDate = v.findViewById(R.id.tvEndDate);
        tvPoints = v.findViewById(R.id.tvRewardPoints);
        tvPointsStatic = v.findViewById(R.id.tvRewardPointsStatic);
        trPoints = v.findViewById(R.id.trPoints);
        
        ivEdit = v.findViewById(R.id.ivEditTask);
        ivConfirm = v.findViewById(R.id.ivConfirmStatus);
        ivCreatorAvatar = v.findViewById(R.id.ivCreatorAvatar);
        ivAssigneeAvatar = v.findViewById(R.id.ivAssigneeAvatar);
        ivBack = v.findViewById(R.id.ivBack);
        
        llPointsSelector = v.findViewById(R.id.llPointsSelector);
        btnPlus = v.findViewById(R.id.btnPlusPoint);
        btnMinus = v.findViewById(R.id.btnMinusPoint);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Xử lý nút Sửa
        ivEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", workspaceId);
            bundle.putString("taskId", taskId);
            Navigation.findNavController(v).navigate(R.id.action_taskDetail_to_editTask, bundle);
        });

        ivConfirm.setOnClickListener(v -> {
            if (currentTask == null) return;

            if (isOwner) {
                if (!Constants.TASK_STATUS_DONE.equals(currentTask.getStatus())) {
                    currentTask.setStatus(Constants.TASK_STATUS_DONE);
                    taskViewModel.updateTask(workspaceId, currentTask);
                    
                    if (currentTask.getAssignedToIds() != null && !currentTask.getAssignedToIds().isEmpty()) {
                        sendNotification(currentTask.getAssignedToIds().get(0), 
                                "Nhiệm vụ đã hoàn thành", 
                                "Chủ phòng đã duyệt nhiệm vụ: " + currentTask.getTitle());
                    }
                }
            } else {
                if (currentTask.getAssignedToIds() != null && currentTask.getAssignedToIds().contains(currentUserId)) {
                    if (!Constants.TASK_STATUS_DONE.equals(currentTask.getStatus()) && !Constants.TASK_STATUS_PENDING.equals(currentTask.getStatus())) {
                        taskViewModel.updateTaskStatus(workspaceId, taskId, Constants.TASK_STATUS_PENDING);
                        
                        sendNotification(currentTask.getCreatedBy(), 
                                "Yêu cầu duyệt nhiệm vụ", 
                                "Thành viên đã hoàn thành và chờ bạn duyệt: " + currentTask.getTitle());
                    }
                }
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (currentTask != null && !Constants.TASK_STATUS_DONE.equals(currentTask.getStatus())) {
                currentTask.setRewardPoints(currentTask.getRewardPoints() + 1);
                taskViewModel.updateTask(workspaceId, currentTask);
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (currentTask != null && currentTask.getRewardPoints() > 0 
                && !Constants.TASK_STATUS_DONE.equals(currentTask.getStatus())) {
                currentTask.setRewardPoints(currentTask.getRewardPoints() - 1);
                taskViewModel.updateTask(workspaceId, currentTask);
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
            }
        });
    }

    private void updateUIForRole() {
        if (currentTask == null) return;
        boolean isDone = Constants.TASK_STATUS_DONE.equals(currentTask.getStatus());

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
            }
        } else {
            ivEdit.setVisibility(View.GONE);
            llPointsSelector.setVisibility(View.GONE);
            tvPointsStatic.setVisibility(View.VISIBLE);
            
            trPoints.setVisibility(isDone ? View.VISIBLE : View.GONE);
            
            if (currentTask.getAssignedToIds() != null && currentTask.getAssignedToIds().contains(currentUserId)) {
                if (!isDone && !Constants.TASK_STATUS_PENDING.equals(currentTask.getStatus())) {
                    ivConfirm.setVisibility(View.VISIBLE);
                } else {
                    ivConfirm.setVisibility(View.GONE);
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
        
        // Logic kiểm tra quá hạn
        long currentTime = System.currentTimeMillis();
        boolean isOverdue = task.getEndDate() != null && 
                           currentTime > task.getEndDate().toDate().getTime() &&
                           !Constants.TASK_STATUS_DONE.equals(task.getStatus()) &&
                           !Constants.TASK_STATUS_PENDING.equals(task.getStatus());

        String statusText = task.getStatus();
        if (isOverdue) {
            tvStatus.setText("QUÁ HẠN");
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF301B1B)); // Đỏ tối
            tvStatus.setTextColor(0xFFD55959); // Đỏ tươi
        } else if (Constants.TASK_STATUS_PENDING.equals(statusText)) {
            tvStatus.setText("CHỜ DUYỆT");
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2A2215));
            tvStatus.setTextColor(0xFFEBB059);
        } else if (Constants.TASK_STATUS_DONE.equals(statusText)) {
            tvStatus.setText("XONG");
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1B3022));
            tvStatus.setTextColor(0xFF59D595);
        } else {
            tvStatus.setText("ĐANG LÀM");
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1B2530));
            tvStatus.setTextColor(0xFF5995D5);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm EEE dd/MM/yyyy", new Locale("vi", "VN"));
        if (task.getStartDate() != null) tvStartDate.setText(sdf.format(task.getStartDate().toDate()));
        if (task.getEndDate() != null) tvEndDate.setText(sdf.format(task.getEndDate().toDate()));

        MutableLiveData<String> creatorNameLiveData = new MutableLiveData<>();
        creatorNameLiveData.observe(getViewLifecycleOwner(), name -> {
            if (name != null) tvCreatorName.setText(name);
        });
        userRepository.getUserName(task.getCreatedBy(), creatorNameLiveData);
        
        if (task.getAssignedToIds() != null && !task.getAssignedToIds().isEmpty()) {
            MutableLiveData<String> assigneeNameLiveData = new MutableLiveData<>();
            assigneeNameLiveData.observe(getViewLifecycleOwner(), name -> {
                if (name != null) tvAssigneeName.setText(name);
            });
            userRepository.getUserName(task.getAssignedToIds().get(0), assigneeNameLiveData);
        }
    }
}
