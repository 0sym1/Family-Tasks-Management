package com.tngoc.familytaskapp.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private RadioGroup rgRepeatType;
    private RadioButton rbDaily, rbWeekly, rbNever, rbAfterTimes, rbAfterDate;
    private LinearLayout llSelectDays;
    private TextView tvTimesCount, tvEndRepeatDate;
    private Button btnConfirm, btnCancelRepeat;
    private View btnBack;
    private int timesCount = 1;
    private Calendar endRepeatCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_repeat, container, false);
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
        llSelectDays = view.findViewById(R.id.sectionDays);
        
        rbNever = view.findViewById(R.id.rbNever);
        rbAfterTimes = view.findViewById(R.id.rbAfterTimes);
        rbAfterDate = view.findViewById(R.id.rbAfterDate);
        
        tvTimesCount = view.findViewById(R.id.tvRepeatCount);
        tvEndRepeatDate = view.findViewById(R.id.tvSelectedDate);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancelRepeat = view.findViewById(R.id.btnCancel);
        btnBack = view.findViewById(R.id.btnBack);

        // Default states
        if (rbDaily != null) rbDaily.setChecked(true);
        if (llSelectDays != null) llSelectDays.setVisibility(View.GONE);
        if (rbNever != null) rbNever.setChecked(true);
    }

    private void setupListeners(View view) {
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        if (btnCancelRepeat != null) btnCancelRepeat.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        if (rgRepeatType != null) {
            rgRepeatType.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbWeekly) {
                    if (llSelectDays != null) llSelectDays.setVisibility(View.VISIBLE);
                } else {
                    if (llSelectDays != null) llSelectDays.setVisibility(View.GONE);
                }
            });
        }

        View btnPlus = view.findViewById(R.id.btnPlus);
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                timesCount++;
                if (tvTimesCount != null) tvTimesCount.setText(String.valueOf(timesCount));
                if (rbAfterTimes != null) rbAfterTimes.setChecked(true);
            });
        }

        View btnMinus = view.findViewById(R.id.btnMinus);
        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (timesCount > 1) {
                    timesCount--;
                    if (tvTimesCount != null) tvTimesCount.setText(String.valueOf(timesCount));
                    if (rbAfterTimes != null) rbAfterTimes.setChecked(true);
                }
            });
        }

        if (tvEndRepeatDate != null) {
            tvEndRepeatDate.setOnClickListener(v -> {
                new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                    endRepeatCalendar.set(Calendar.YEAR, year);
                    endRepeatCalendar.set(Calendar.MONTH, month);
                    endRepeatCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvEndRepeatDate.setText(dateFormat.format(endRepeatCalendar.getTime()));
                    if (rbAfterDate != null) rbAfterDate.setChecked(true);
                }, endRepeatCalendar.get(Calendar.YEAR), endRepeatCalendar.get(Calendar.MONTH), endRepeatCalendar.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Navigation.findNavController(v).navigateUp();
            });
        }
    }
}
