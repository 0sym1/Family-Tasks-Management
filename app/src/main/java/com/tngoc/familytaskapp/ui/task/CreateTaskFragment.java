package com.tngoc.familytaskapp.ui.task;

import android.app.DatePickerDialog;
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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CreateTaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private WorkspaceViewModel workspaceViewModel;
    
    private EditText etTaskName;
    private Spinner spinnerStatus, spinnerAssignedTo;
    private TextView tvStartDate, tvEndDate, tvCreatedBy, tvTitle;
    private RadioGroup rgRepeat;
    private RadioButton rbRepeatOn, rbRepeatOff;
    private ImageView ivEditRepeat;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    
    private String workspaceId;
    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private List<String> memberIds = new ArrayList<>();

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
        }

        initViews(view);
        setupSpinners();
        setupDatePickers();
        setupListeners();

        tvCreatedBy.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : 
                FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if (workspaceId != null) {
            workspaceViewModel.loadWorkspace(workspaceId);
        }

        observeViewModel();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.btnBack);
        etTaskName = view.findViewById(R.id.etTaskName);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        spinnerAssignedTo = view.findViewById(R.id.spinnerAssignedTo);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvCreatedBy = view.findViewById(R.id.tvCreatedBy);
        rgRepeat = view.findViewById(R.id.rgRepeat);
        rbRepeatOn = view.findViewById(R.id.rbRepeatOn);
        rbRepeatOff = view.findViewById(R.id.rbRepeatOff);
        ivEditRepeat = view.findViewById(R.id.ivEditRepeat);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        
        // Initial visibility
        updateRepeatIconVisibility();
    }

    private void setupSpinners() {
        String[] statuses = {getString(R.string.status_doing), getString(R.string.status_done)};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, statuses);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupDatePickers() {
        View.OnClickListener startPicker = v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
                tvStartDate.setTextColor(getResources().getColor(R.color.white));
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        View.OnClickListener endPicker = v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
                tvEndDate.setTextColor(getResources().getColor(R.color.white));
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        requireView().findViewById(R.id.llStartDate).setOnClickListener(startPicker);
        requireView().findViewById(R.id.llEndDate).setOnClickListener(endPicker);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        rgRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            updateRepeatIconVisibility();
        });

        btnSave.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            String statusValue = spinnerStatus.getSelectedItemPosition() == 0 ? "doing" : "done";
            boolean isRepeat = rbRepeatOn.isChecked();
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // For now, assigning to the selected single member (can be extended to multiple)
            String assignedMemberId = memberIds.isEmpty() ? currentUserId : memberIds.get(spinnerAssignedTo.getSelectedItemPosition());

            Task task = new Task(
                    workspaceId,
                    name,
                    "",
                    Collections.singletonList(assignedMemberId),
                    currentUserId,
                    statusValue,
                    0,
                    new Timestamp(startCalendar.getTime()),
                    new Timestamp(endCalendar.getTime()),
                    isRepeat
            );

            taskViewModel.createTask(workspaceId, task);
        });
    }
    
    private void updateRepeatIconVisibility() {
        if (ivEditRepeat != null) {
            ivEditRepeat.setVisibility(rbRepeatOn.isChecked() ? View.VISIBLE : View.GONE);
        }
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null && workspace.getMemberIds() != null) {
                memberIds = workspace.getMemberIds();
                // Ideally, we should fetch member names here. For now, showing IDs or a placeholder.
                ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, memberIds);
                memberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerAssignedTo.setAdapter(memberAdapter);
            }
        });

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
