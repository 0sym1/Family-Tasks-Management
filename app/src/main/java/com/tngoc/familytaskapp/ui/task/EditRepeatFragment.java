package com.tngoc.familytaskapp.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.tngoc.familytaskapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditRepeatFragment extends Fragment {

    private TaskViewModel taskViewModel;

    private int repeatCount = 0;
    private Calendar selectedDate = Calendar.getInstance();
    private boolean hasPickedDate = false;

    private RadioGroup rgRepeatType;
    private RadioButton rbDaily, rbWeekly;
    private RadioButton rbNever, rbAfterTimes, rbAfterDate;
    private LinearLayout sectionDays;

    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;

    private TextView tvRepeatCount, tvSelectedDate;
    private TextView btnMinus, btnPlus;
    private TextView tvLabelNever, tvLabelAfter, tvLabelTimes, tvLabelAfterDate;

    private MaterialButton btnConfirm, btnCancel;

    public EditRepeatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_repeat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Share ViewModel với CreateTaskFragment
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        initViews(view);
        setupClicks(view);
        loadCurrentSettings();
        
        // Initial state visibility
        updateRepeatTypeVisibility(rgRepeatType.getCheckedRadioButtonId());
        updateEndOptionStyles();
    }

    private void initViews(View view) {
        rgRepeatType = view.findViewById(R.id.rgRepeatType);
        rbDaily = view.findViewById(R.id.rbDaily);
        rbWeekly = view.findViewById(R.id.rbWeekly);
        sectionDays = view.findViewById(R.id.sectionDays);

        rbNever = view.findViewById(R.id.rbNever);
        rbAfterTimes = view.findViewById(R.id.rbAfterTimes);
        rbAfterDate = view.findViewById(R.id.rbAfterDate);

        tvLabelNever = view.findViewById(R.id.tvLabelNever);
        tvLabelAfter = view.findViewById(R.id.tvLabelAfter);
        tvLabelTimes = view.findViewById(R.id.tvLabelTimes);
        tvLabelAfterDate = view.findViewById(R.id.tvLabelAfterDate);

        cbMon = view.findViewById(R.id.cbMon);
        cbTue = view.findViewById(R.id.cbTue);
        cbWed = view.findViewById(R.id.cbWed);
        cbThu = view.findViewById(R.id.cbThu);
        cbFri = view.findViewById(R.id.cbFri);
        cbSat = view.findViewById(R.id.cbSat);
        cbSun = view.findViewById(R.id.cbSun);

        tvRepeatCount = view.findViewById(R.id.tvRepeatCount);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        btnMinus = view.findViewById(R.id.btnMinus);
        btnPlus = view.findViewById(R.id.btnPlus);

        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancel = view.findViewById(R.id.btnCancel);
        
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
    }

    private void setupClicks(View view) {

        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            updateRepeatTypeVisibility(checkedId);
        });

        rbNever.setOnClickListener(v -> selectEndOption(rbNever));
        rbAfterTimes.setOnClickListener(v -> selectEndOption(rbAfterTimes));
        rbAfterDate.setOnClickListener(v -> selectEndOption(rbAfterDate));
        
        tvLabelNever.setOnClickListener(v -> selectEndOption(rbNever));
        tvLabelAfter.setOnClickListener(v -> selectEndOption(rbAfterTimes));
        tvLabelAfterDate.setOnClickListener(v -> selectEndOption(rbAfterDate));

        btnMinus.setOnClickListener(v -> {
            if (repeatCount > 0) {
                repeatCount--;
                tvRepeatCount.setText(String.valueOf(repeatCount));
                if (!rbAfterTimes.isChecked()) selectEndOption(rbAfterTimes);
            }
        });

        btnPlus.setOnClickListener(v -> {
            repeatCount++;
            tvRepeatCount.setText(String.valueOf(repeatCount));
            if (!rbAfterTimes.isChecked()) selectEndOption(rbAfterTimes);
        });

        tvSelectedDate.setOnClickListener(v -> {
            if (!rbAfterDate.isChecked()) selectEndOption(rbAfterDate);
            
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        hasPickedDate = true;

                        tvSelectedDate.setText(
                                String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                        dayOfMonth, month + 1, year)
                        );
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        btnConfirm.setOnClickListener(v -> saveSettings());
    }

    private void loadCurrentSettings() {
        // Load data from TaskViewModel if available
        Boolean isRep = taskViewModel.isRepeating.getValue();
        if (isRep != null && isRep) {
            String type = taskViewModel.repeatType.getValue();
            if ("Daily".equals(type)) rbDaily.setChecked(true);
            else rbWeekly.setChecked(true);

            List<String> days = taskViewModel.repeatDays.getValue();
            if (days != null) {
                cbMon.setChecked(days.contains("mon"));
                cbTue.setChecked(days.contains("tue"));
                cbWed.setChecked(days.contains("wed"));
                cbThu.setChecked(days.contains("thu"));
                cbFri.setChecked(days.contains("fri"));
                cbSat.setChecked(days.contains("sat"));
                cbSun.setChecked(days.contains("sun"));
            }

            String endType = taskViewModel.repeatEndType.getValue();
            if ("never".equals(endType)) selectEndOption(rbNever);
            else if ("count".equals(endType)) {
                selectEndOption(rbAfterTimes);
                repeatCount = taskViewModel.repeatCount.getValue() != null ? taskViewModel.repeatCount.getValue() : 0;
                tvRepeatCount.setText(String.valueOf(repeatCount));
            } else if ("date".equals(endType)) {
                selectEndOption(rbAfterDate);
                Timestamp until = taskViewModel.repeatUntil.getValue();
                if (until != null) {
                    selectedDate.setTime(until.toDate());
                    hasPickedDate = true;
                    tvSelectedDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedDate.get(Calendar.DAY_OF_MONTH),
                            selectedDate.get(Calendar.MONTH) + 1,
                            selectedDate.get(Calendar.YEAR)));
                }
            }
        }
    }

    private void saveSettings() {
        String frequency = rbDaily.isChecked() ? "Daily" : "Weekly";
        List<String> weekDays = new ArrayList<>();

        if (rbWeekly.isChecked()) {
            if (cbMon.isChecked()) weekDays.add("mon");
            if (cbTue.isChecked()) weekDays.add("tue");
            if (cbWed.isChecked()) weekDays.add("wed");
            if (cbThu.isChecked()) weekDays.add("thu");
            if (cbFri.isChecked()) weekDays.add("fri");
            if (cbSat.isChecked()) weekDays.add("sat");
            if (cbSun.isChecked()) weekDays.add("sun");

            if (weekDays.isEmpty()) {
                Toast.makeText(requireContext(), "Chọn ít nhất 1 ngày", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String endType = "never";
        if (rbAfterTimes.isChecked()) endType = "count";
        else if (rbAfterDate.isChecked()) endType = "date";

        // Save to Shared ViewModel
        taskViewModel.isRepeating.setValue(true);
        taskViewModel.repeatType.setValue(frequency);
        taskViewModel.repeatDays.setValue(weekDays);
        taskViewModel.repeatEndType.setValue(endType);
        taskViewModel.repeatCount.setValue(repeatCount);
        if (hasPickedDate) {
            taskViewModel.repeatUntil.setValue(new Timestamp(selectedDate.getTime()));
        }

        Toast.makeText(requireContext(), "Đã lưu thiết lập lặp lại", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void updateRepeatTypeVisibility(int checkedId) {
        if (checkedId == R.id.rbDaily) {
            sectionDays.setVisibility(View.GONE);
        } else {
            sectionDays.setVisibility(View.VISIBLE);
        }
    }

    private void selectEndOption(RadioButton selected) {
        rbNever.setChecked(selected == rbNever);
        rbAfterTimes.setChecked(selected == rbAfterTimes);
        rbAfterDate.setChecked(selected == rbAfterDate);
        updateEndOptionStyles();
    }

    private void updateEndOptionStyles() {
        if (getContext() == null) return;
        int activeColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        if (tvLabelNever != null) tvLabelNever.setTextColor(rbNever.isChecked() ? activeColor : inactiveColor);
        if (tvLabelAfter != null) tvLabelAfter.setTextColor(rbAfterTimes.isChecked() ? activeColor : inactiveColor);
        if (tvLabelTimes != null) tvLabelTimes.setTextColor(rbAfterTimes.isChecked() ? activeColor : inactiveColor);
        if (tvRepeatCount != null) tvRepeatCount.setTextColor(rbAfterTimes.isChecked() ? activeColor : inactiveColor);
        if (btnMinus != null) btnMinus.setTextColor(rbAfterTimes.isChecked() ? activeColor : inactiveColor);
        if (btnPlus != null) btnPlus.setTextColor(rbAfterTimes.isChecked() ? activeColor : inactiveColor);
        if (tvLabelAfterDate != null) tvLabelAfterDate.setTextColor(rbAfterDate.isChecked() ? activeColor : inactiveColor);
        if (tvSelectedDate != null) tvSelectedDate.setTextColor(rbAfterDate.isChecked() ? activeColor : inactiveColor);
    }
}
