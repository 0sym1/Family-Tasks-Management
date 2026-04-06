package com.tngoc.familytaskapp.ui.task;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public interface OnDatesSelectedListener {
        void onDatesSelected(Set<Date> selectedDates);
        void onMonthYearChanged(int month, int year);
    }

    private List<Date> dates = new ArrayList<>();
    private Set<Date> selectedDates = new HashSet<>();
    private final OnDatesSelectedListener listener;
    private final String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
    private Calendar currentStartCal;

    public CalendarAdapter(OnDatesSelectedListener listener) {
        this.listener = listener;
        currentStartCal = Calendar.getInstance();
        currentStartCal.add(Calendar.DAY_OF_YEAR, -2); // Today will be at index 2 (middle of 5)
        generateDates();
        
        // Initially select today
        Calendar today = Calendar.getInstance();
        normalizeCalendar(today);
        selectedDates.add(today.getTime());
    }

    private void generateDates() {
        dates.clear();
        Calendar tempCal = (Calendar) currentStartCal.clone();
        for (int i = 0; i < 5; i++) {
            dates.add(tempCal.getTime());
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }
        notifyDataSetChanged();
        updateMonthYear();
    }

    private void updateMonthYear() {
        Map<String, Integer> monthCounts = new HashMap<>();
        Map<String, Integer> yearCounts = new HashMap<>();
        
        Calendar cal = Calendar.getInstance();
        for (Date date : dates) {
            cal.setTime(date);
            String monthKey = String.valueOf(cal.get(Calendar.MONTH));
            String yearKey = String.valueOf(cal.get(Calendar.YEAR));
            
            monthCounts.put(monthKey, monthCounts.getOrDefault(monthKey, 0) + 1);
            yearCounts.put(yearKey, yearCounts.getOrDefault(yearKey, 0) + 1);
        }
        
        int majorityMonth = -1;
        int maxMonthCount = -1;
        for (Map.Entry<String, Integer> entry : monthCounts.entrySet()) {
            if (entry.getValue() > maxMonthCount) {
                maxMonthCount = entry.getValue();
                majorityMonth = Integer.parseInt(entry.getKey());
            }
        }
        
        int majorityYear = -1;
        int maxYearCount = -1;
        for (Map.Entry<String, Integer> entry : yearCounts.entrySet()) {
            if (entry.getValue() > maxYearCount) {
                maxYearCount = entry.getValue();
                majorityYear = Integer.parseInt(entry.getKey());
            }
        }
        
        if (listener != null) {
            listener.onMonthYearChanged(majorityMonth, majorityYear);
        }
    }

    public void nextWeek() {
        currentStartCal.add(Calendar.DAY_OF_YEAR, 5);
        generateDates();
    }

    public void prevWeek() {
        currentStartCal.add(Calendar.DAY_OF_YEAR, -5);
        generateDates();
    }

    private void notifyListener() {
        listener.onDatesSelected(selectedDates);
    }

    private void normalizeCalendar(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Date date = dates.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        normalizeCalendar(cal);
        Date normalizedDate = cal.getTime();

        holder.tvDayName.setText(dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]);
        holder.tvDateNumber.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

        if (selectedDates.contains(normalizedDate)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_calendar_selected);
            holder.tvDayName.setTextColor(Color.WHITE);
            holder.tvDateNumber.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.tvDayName.setTextColor(Color.parseColor("#888888"));
            holder.tvDateNumber.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedDates.contains(normalizedDate)) {
                selectedDates.remove(normalizedDate);
            } else {
                selectedDates.add(normalizedDate);
            }
            notifyItemChanged(position);
            notifyListener();
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDateNumber;
        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDateNumber = itemView.findViewById(R.id.tvDateNumber);
        }
    }
}
