package com.tngoc.familytaskapp.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
    private Spinner spinnerStatus;
    private TextView tvStartDate, tvEndDate, tvTitle, tvAssignedTo;
    private RadioGroup rgRepeat;
    private RadioButton rbRepeatOn, rbRepeatOff;
    private ImageView ivEditRepeat;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private View llAssignedTo;
    
    private String workspaceId;
    private String taskId;
    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
            taskId = getArguments().getString("taskId");
        }

        initViews(view);
        setupSpinners();
        setupDatePickers();
        setupListeners();

        if (workspaceId != null) {
            workspaceViewModel.loadWorkspace(workspaceId);
        }

        if (taskId != null) {
            tvTitle.setText("Edit Task");
            taskViewModel.loadTask(workspaceId, taskId);
        }

        observeViewModel();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.btnBack);
        etTaskName = view.findViewById(R.id.etTaskName);
        etTaskDescription = view.findViewById(R.id.etTaskDescription);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        llAssignedTo = view.findViewById(R.id.llAssignedTo);
        tvAssignedTo = view.findViewById(R.id.tvAssignedTo);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        rgRepeat = view.findViewById(R.id.rgRepeat);
        rbRepeatOn = view.findViewById(R.id.rbRepeatOn);
        rbRepeatOff = view.findViewById(R.id.rbRepeatOff);
        ivEditRepeat = view.findViewById(R.id.ivEditRepeat);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        
        updateRepeatIconVisibility();
    }

    private void setupSpinners() {
        String[] statuses = {getString(R.string.status_doing), "Done", "Pending"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, statuses);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
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
                tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
                tvStartDate.setTextColor(getResources().getColor(R.color.white));
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
                tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
                tvEndDate.setTextColor(getResources().getColor(R.color.white));
            });
            datePicker.show(getParentFragmentManager(), "END_DATE_PICKER");
        };

        requireView().findViewById(R.id.llStartDate).setOnClickListener(startPickerListener);
        requireView().findViewById(R.id.llEndDate).setOnClickListener(endPickerListener);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        rgRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            updateRepeatIconVisibility();
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

            String statusValue;
            int pos = spinnerStatus.getSelectedItemPosition();
            if (pos == 0) statusValue = Constants.TASK_STATUS_IN_PROGRESS;
            else if (pos == 1) statusValue = Constants.TASK_STATUS_DONE;
            else statusValue = Constants.TASK_STATUS_PENDING;

            boolean isRepeat = rbRepeatOn.isChecked();

            if (taskId == null) {
                // Create
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Task task = new Task(
                        workspaceId,
                        name,
                        description,
                        selectedMemberIds,
                        currentUserId,
                        statusValue,
                        0,
                        new Timestamp(startCalendar.getTime()),
                        new Timestamp(endCalendar.getTime()),
                        isRepeat
                );
                taskViewModel.createTask(workspaceId, task);
            } else {
                // Update
                if (mTask != null) {
                    mTask.setTitle(name);
                    mTask.setDescription(description);
                    mTask.setAssignedToIds(selectedMemberIds);
                    mTask.setStatus(statusValue);
                    mTask.setStartDate(new Timestamp(startCalendar.getTime()));
                    mTask.setEndDate(new Timestamp(endCalendar.getTime()));
                    mTask.setRepeat(isRepeat);
                    taskViewModel.updateTask(workspaceId, mTask);
                }
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
        if (ivEditRepeat != null) {
            ivEditRepeat.setVisibility(rbRepeatOn.isChecked() ? View.VISIBLE : View.GONE);
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
                if (taskId != null && mTask != null) {
                    updateAssignedToUI();
                } else {
                    this.checkedItems = new boolean[members.size()];
                }
            }
        });

        taskViewModel.taskLiveData.observe(getViewLifecycleOwner(), task -> {
            if (task != null && taskId != null) {
                this.mTask = task;
                etTaskName.setText(task.getTitle());
                etTaskDescription.setText(task.getDescription());
                
                if (Constants.TASK_STATUS_IN_PROGRESS.equals(task.getStatus())) spinnerStatus.setSelection(0);
                else if (Constants.TASK_STATUS_DONE.equals(task.getStatus())) spinnerStatus.setSelection(1);
                else spinnerStatus.setSelection(2);

                if (task.getStartDate() != null) {
                    startCalendar.setTime(task.getStartDate().toDate());
                    tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
                    tvStartDate.setTextColor(getResources().getColor(R.color.white));
                }
                if (task.getEndDate() != null) {
                    endCalendar.setTime(task.getEndDate().toDate());
                    tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
                    tvEndDate.setTextColor(getResources().getColor(R.color.white));
                }

                if (task.isRepeat()) rbRepeatOn.setChecked(true);
                else rbRepeatOff.setChecked(true);

                selectedMemberIds = new ArrayList<>(task.getAssignedToIds());
                if (!workspaceMembers.isEmpty()) {
                    updateAssignedToUI();
                }
            }
        });

        taskViewModel.taskIdLiveData.observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Toast.makeText(requireContext(), getString(R.string.task_created), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        taskViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        taskViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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
