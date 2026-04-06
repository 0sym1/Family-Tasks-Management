package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;
import com.tngoc.familytaskapp.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CreateTaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private WorkspaceViewModel workspaceViewModel;
    
    private EditText etTaskName, etTaskDescription;
    private TextView tvStartDate, tvEndDate, tvEndTime, tvTitle, tvAssignedTo;
    private RadioGroup rgRepeat;
    private RadioButton rbRepeatOn, rbRepeatOff;
    private ImageView ivEditRepeat;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private View llAssignedTo, llEndDate, llEndTime;
    
    private String workspaceId;
    private String taskId;
    private Calendar startCalendar, endCalendar, endTimeCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private List<User> workspaceMembers = new ArrayList<>();
    private List<String> selectedMemberIds = new ArrayList<>();
    private boolean[] checkedItems;
    private Task mTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        if (savedInstanceState == null && !taskViewModel.isEditModeInitialized) {
            taskViewModel.resetRepeatSettings();
        }

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
            taskId = getArguments().getString("taskId");
            suggestedTaskName = getArguments().getString("suggestedTaskName");
        }

        initViews(view);
        setupDatePickers();
        setupListeners();

        if (workspaceId != null) {
            workspaceViewModel.loadWorkspace(workspaceId);
        }

        if (taskId != null) {
            tvTitle.setText(R.string.edit_task);
            if (!taskViewModel.isEditModeInitialized) {
                taskViewModel.loadTask(workspaceId, taskId);
            }
        }

        observeViewModel();
        updateRepeatUIFromViewModel();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.btnBack);
        etTaskName = view.findViewById(R.id.etTaskName);
        etTaskDescription = view.findViewById(R.id.etTaskDescription);
        llAssignedTo = view.findViewById(R.id.llAssignedTo);
        llEndDate = view.findViewById(R.id.llEndDate);
        llEndTime = view.findViewById(R.id.llEndTime);
        tvAssignedTo = view.findViewById(R.id.tvAssignedTo);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        rgRepeat = view.findViewById(R.id.rgRepeat);
        rbRepeatOn = rbRepeatOn != null ? rbRepeatOn : view.findViewById(R.id.rbRepeatOn);
        rbRepeatOff = rbRepeatOff != null ? rbRepeatOff : view.findViewById(R.id.rbRepeatOff);
        ivEditRepeat = view.findViewById(R.id.ivEditRepeat);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        startCalendar = Calendar.getInstance();
        normalizeCalendar(startCalendar);
        
        endCalendar = Calendar.getInstance();
        normalizeCalendar(endCalendar);

        endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, 12);
        endTimeCalendar.set(Calendar.MINUTE, 0);
        
        tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
        tvEndTime.setText(timeFormat.format(endTimeCalendar.getTime()));
        
        updateRepeatIconVisibility();
    }

    private void normalizeCalendar(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setupDatePickers() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        View.OnClickListener startPickerListener = v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày bắt đầu")
                    .setSelection(startCalendar.getTimeInMillis())
                    .setCalendarConstraints(constraints)
                    .setTheme(R.style.CustomDatePickerTheme)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                startCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                normalizeCalendar(startCalendar);
                tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
            });
            datePicker.show(getParentFragmentManager(), "START_DATE_PICKER");
        };

        View.OnClickListener endPickerListener = v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .setSelection(endCalendar.getTimeInMillis())
                    .setCalendarConstraints(constraints)
                    .setTheme(R.style.CustomDatePickerTheme)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                endCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                normalizeCalendar(endCalendar);
                tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
            });
            datePicker.show(getParentFragmentManager(), "END_DATE_PICKER");
        };

        View.OnClickListener timePickerListener = v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(endTimeCalendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(endTimeCalendar.get(Calendar.MINUTE))
                    .setTitleText("Chọn giờ kết thúc")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v1 -> {
                endTimeCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                endTimeCalendar.set(Calendar.MINUTE, timePicker.getMinute());
                tvEndTime.setText(timeFormat.format(endTimeCalendar.getTime()));
            });
            timePicker.show(getParentFragmentManager(), "END_TIME_PICKER");
        };

        View llStartDateContainer = requireView().findViewById(R.id.llStartDate);
        if (llStartDateContainer != null) llStartDateContainer.setOnClickListener(startPickerListener);
        if (llEndDate != null) llEndDate.setOnClickListener(endPickerListener);
        if (llEndTime != null) llEndTime.setOnClickListener(timePickerListener);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        rgRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isRepeat = (checkedId == R.id.rbRepeatOn);
            taskViewModel.isRepeating.setValue(isRepeat);
            
            if (isRepeat && taskViewModel.repeatType.getValue() == null) {
                taskViewModel.repeatType.setValue("Daily");
                taskViewModel.repeatEndType.setValue("never");
            }
            
            updateRepeatIconVisibility();
        });

        ivEditRepeat.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_createTask_to_editRepeat);
        });

        llAssignedTo.setOnClickListener(v -> showMultiSelectDialog());

        btnSave.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedMemberIds.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ít nhất một người thực hiện", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);

            String statusValue = (taskId == null) ? Constants.TASK_STATUS_IN_PROGRESS : mTask.getStatus();
            boolean isRepeat = rbRepeatOn.isChecked();
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Task task = (taskId == null) ? new Task() : mTask;
            
            task.setWorkspaceId(workspaceId);
            task.setTitle(name);
            task.setDescription(description);
            task.setAssignedToIds(selectedMemberIds);
            task.setStatus(statusValue);
            
            normalizeCalendar(startCalendar);
            task.setStartDate(new Timestamp(startCalendar.getTime()));
            task.setEndTime(tvEndTime.getText().toString());
            
            if (isRepeat) {
                task.setRepeat(true);
                task.setRepeating(true);
                task.setRepeatType(taskViewModel.repeatType.getValue());
                task.setRepeatDays(taskViewModel.repeatDays.getValue());
                task.setRepeatEndType(taskViewModel.repeatEndType.getValue());
                task.setRepeatCount(taskViewModel.repeatCount.getValue() != null ? taskViewModel.repeatCount.getValue() : 0);
                task.setRepeatUntil(taskViewModel.repeatUntil.getValue());

                String endType = taskViewModel.repeatEndType.getValue();
                if ("date".equals(endType)) {
                    Timestamp repeatUntil = taskViewModel.repeatUntil.getValue();
                    if (repeatUntil != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(repeatUntil.toDate());
                        normalizeCalendar(cal);
                        task.setEndDate(new Timestamp(cal.getTime()));
                    } else {
                        task.setEndDate(null);
                    }
                } else {
                    task.setEndDate(null);
                }
            } else {
                task.setRepeat(false);
                task.setRepeating(false);
                normalizeCalendar(endCalendar);
                task.setEndDate(new Timestamp(endCalendar.getTime()));
                task.setRepeatUntil(null);
                task.setRepeatType(null);
                task.setRepeatDays(null);
                task.setRepeatEndType(null);
                task.setRepeatCount(0);
            }
            
            TaskViewModel.OnTaskActionListener actionListener = new TaskViewModel.OnTaskActionListener() {
                @Override
                public void onSuccess(String newId) {
                    if (isAdded()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("workspaceId", workspaceId);
                        bundle.putString("taskId", newId);
                        taskViewModel.resetRepeatSettings();
                        NavHostFragment.findNavController(CreateTaskFragment.this)
                                .navigate(R.id.action_createTask_to_taskDetail, bundle);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (isAdded()) {
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            };

            if (taskId == null) {
                task.setCreatedBy(currentUserId);
                task.setCreatedAt(Timestamp.now());
                taskViewModel.createTask(workspaceId, task, actionListener);
            } else {
                taskViewModel.updateTask(workspaceId, task, actionListener);
            }
        });
    }

    private void showMultiSelectDialog() {
        if (workspaceMembers.isEmpty()) return;

        String[] memberNames = new String[workspaceMembers.size()];
        for (int i = 0; i < workspaceMembers.size(); i++) {
            User user = workspaceMembers.get(i);
            memberNames[i] = user.getDisplayName() != null && !user.getDisplayName().isEmpty() 
                    ? user.getDisplayName() : user.getEmail();
        }

        if (checkedItems == null) {
            checkedItems = new boolean[workspaceMembers.size()];
        }

        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogTheme)
                .setTitle("Chọn người thực hiện")
                .setMultiChoiceItems(memberNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Xong", (dialog, which) -> {
                    selectedMemberIds.clear();
                    StringBuilder selectedNames = new StringBuilder();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedMemberIds.add(workspaceMembers.get(i).getUserId());
                            if (selectedNames.length() > 0) selectedNames.append(", ");
                            selectedNames.append(memberNames[i]);
                        }
                    }
                    tvAssignedTo.setText(selectedNames.length() > 0 ? selectedNames.toString() : "Chọn người làm...");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void updateRepeatIconVisibility() {
        boolean isRepeat = rbRepeatOn.isChecked();
        if (ivEditRepeat != null) ivEditRepeat.setVisibility(isRepeat ? View.VISIBLE : View.GONE);
        if (llEndDate != null) llEndDate.setVisibility(isRepeat ? View.GONE : View.VISIBLE);
    }

    private void updateRepeatUIFromViewModel() {
        Boolean isRepeating = taskViewModel.isRepeating.getValue();
        if (isRepeating != null) {
            if (isRepeating) rbRepeatOn.setChecked(true);
            else rbRepeatOff.setChecked(true);
            updateRepeatIconVisibility();
        }
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null && workspace.getMemberIds() != null) {
                workspaceViewModel.loadMembers(workspace.getMemberIds());
            }
        });

        workspaceViewModel.membersLiveData.observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                this.workspaceMembers = members;
                if (taskId != null && mTask != null) updateAssignedToUI();
                else this.checkedItems = new boolean[members.size()];
            }
        });

        taskViewModel.taskLiveData.observe(getViewLifecycleOwner(), task -> {
            if (task != null && taskId != null && !taskViewModel.isEditModeInitialized) {
                this.mTask = task;
                etTaskName.setText(task.getTitle());
                etTaskDescription.setText(task.getDescription());

                if (task.getStartDate() != null) {
                    startCalendar.setTime(task.getStartDate().toDate());
                    normalizeCalendar(startCalendar);
                    tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
                }
                
                if (task.getEndTime() != null) {
                    tvEndTime.setText(task.getEndTime());
                    try {
                        String[] parts = task.getEndTime().split(":");
                        endTimeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                        endTimeCalendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    } catch (Exception e) {}
                }

                if (task.getEndDate() != null) {
                    endCalendar.setTime(task.getEndDate().toDate());
                    normalizeCalendar(endCalendar);
                    tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
                }

                boolean repeatState = task.isRepeat() || task.isRepeating();
                if (repeatState) rbRepeatOn.setChecked(true);
                else rbRepeatOff.setChecked(true);
                
                updateRepeatIconVisibility();

                selectedMemberIds = new ArrayList<>(task.getAssignedToIds());
                if (!workspaceMembers.isEmpty()) updateAssignedToUI();

                taskViewModel.isRepeating.setValue(repeatState);
                taskViewModel.repeatType.setValue(task.getRepeatType());
                taskViewModel.repeatDays.setValue(task.getRepeatDays());
                taskViewModel.repeatEndType.setValue(task.getRepeatEndType());
                taskViewModel.repeatCount.setValue(task.getRepeatCount());
                taskViewModel.repeatUntil.setValue(task.getRepeatUntil());
                
                taskViewModel.isEditModeInitialized = true;
            }
        });
    }

    private void updateAssignedToUI() {
        checkedItems = new boolean[workspaceMembers.size()];
        StringBuilder selectedNames = new StringBuilder();
        for (int i = 0; i < workspaceMembers.size(); i++) {
            if (selectedMemberIds.contains(workspaceMembers.get(i).getUserId())) {
                checkedItems[i] = true;
                if (selectedNames.length() > 0) selectedNames.append(", ");
                selectedNames.append(workspaceMembers.get(i).getDisplayName());
            }
        }
        tvAssignedTo.setText(selectedNames.length() > 0 ? selectedNames.toString() : "Chọn người làm...");
    }
}
