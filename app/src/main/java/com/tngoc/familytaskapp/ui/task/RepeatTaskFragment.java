package com.tngoc.familytaskapp.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tngoc.familytaskapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RepeatTaskFragment extends Fragment {

    private RadioGroup rgRepeatType, rgEndRepeat;
    private RadioButton rbDaily, rbWeekly, rbNever, rbAfterTimes, rbAfterDate;
    private LinearLayout llSelectDays;
    private TextView tvTimesCount, tvEndRepeatDate;
    private Button btnConfirm, btnCancelRepeat;
    private ImageButton btnBack;
    private int timesCount = 1;
    private Calendar endRepeatCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_repeat_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners(view);
    }

    private void initViews(View view) {
        rgRepeatType = view.findViewById(R.id.rgRepeatType);
        rbDaily = view.findViewById(R.id.rbDaily);
        rbWeekly = view.findViewById(R.id.rbWeekly);
        llSelectDays = view.findViewById(R.id.llSelectDays);
        rgEndRepeat = view.findViewById(R.id.rgEndRepeat);
        rbNever = view.findViewById(R.id.rbNever);
        rbAfterTimes = view.findViewById(R.id.rbAfterTimes);
        rbAfterDate = view.findViewById(R.id.rbAfterDate);
        tvTimesCount = view.findViewById(R.id.tvTimesCount);
        tvEndRepeatDate = view.findViewById(R.id.tvEndRepeatDate);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancelRepeat = view.findViewById(R.id.btnCancelRepeat);
        btnBack = view.findViewById(R.id.btnBack);

        // Default states
        rbDaily.setChecked(true);
        llSelectDays.setVisibility(View.GONE);
        rbNever.setChecked(true);
    }

    private void setupListeners(View view) {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnCancelRepeat.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbWeekly) {
                llSelectDays.setVisibility(View.VISIBLE);
            } else {
                llSelectDays.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            timesCount++;
            tvTimesCount.setText(String.valueOf(timesCount));
            rbAfterTimes.setChecked(true);
        });

        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (timesCount > 1) {
                timesCount--;
                tvTimesCount.setText(String.valueOf(timesCount));
                rbAfterTimes.setChecked(true);
            }
        });

        tvEndRepeatDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                endRepeatCalendar.set(Calendar.YEAR, year);
                endRepeatCalendar.set(Calendar.MONTH, month);
                endRepeatCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvEndRepeatDate.setText(dateFormat.format(endRepeatCalendar.getTime()));
                rbAfterDate.setChecked(true);
            }, endRepeatCalendar.get(Calendar.YEAR), endRepeatCalendar.get(Calendar.MONTH), endRepeatCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnConfirm.setOnClickListener(v -> {
            // Logic to save repeat settings can be added here
            Navigation.findNavController(v).navigateUp();
        });
    }
}
