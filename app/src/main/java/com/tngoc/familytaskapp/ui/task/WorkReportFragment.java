package com.tngoc.familytaskapp.ui.task;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkReportFragment extends Fragment {

    private TextView tvWeeklyTasks, tvMonthlyTasks, tvTotalWorkTime;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private ImageView btnBack;
    
    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration listenerRegistration;
    private Map<String, String> workspaceNameMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_work_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        tvWeeklyTasks = view.findViewById(R.id.tvWeeklyTasks);
        tvMonthlyTasks = view.findViewById(R.id.tvMonthlyTasks);
        tvTotalWorkTime = view.findViewById(R.id.tvTotalWorkTime);
        
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);
        btnBack = view.findViewById(R.id.btnBack);

        setupCharts();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        fetchWorkspacesAndStartListening();
    }

    private void setupCharts() {
        // Pie Chart
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(40, 10, 40, 10);
        
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(70f); 
        pieChart.setTransparentCircleRadius(0f);
        
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(14f);
        
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setRotationEnabled(true);

        // Bar Chart
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#22FFFFFF"));
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setNoDataText("Đang tải dữ liệu...");

        // Line Chart
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.parseColor("#22FFFFFF"));
        leftAxis.setAxisMinimum(0f);
        
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP);
        lineChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT);
        lineChart.setNoDataText("Đang tải dữ liệu...");
    }

    private void fetchWorkspacesAndStartListening() {
        if (currentUserId == null) return;

        db.collection(Constants.COLLECTION_WORKSPACES)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    workspaceNameMap.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        workspaceNameMap.put(doc.getId(), doc.getString("name"));
                    }
                    if (listenerRegistration == null) {
                        startListening();
                    }
                });
    }

    private void startListening() {
        if (currentUserId == null) return;

        listenerRegistration = db.collectionGroup(Constants.COLLECTION_TASKS)
                .whereArrayContains("assignedToIds", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    int weeklyCount = 0, monthlyCount = 0;
                    long totalTimeMinutes = 0;
                    
                    Map<String, Long> workspaceTimeMap = new HashMap<>();
                    int[] dailyDone = new int[7];
                    int[] dailyTotal = new int[7];

                    Calendar now = Calendar.getInstance();
                    
                    Calendar tenDaysAgo = Calendar.getInstance();
                    tenDaysAgo.add(Calendar.DAY_OF_YEAR, -9); // 10 days including today
                    tenDaysAgo.set(Calendar.HOUR_OF_DAY, 0);
                    tenDaysAgo.set(Calendar.MINUTE, 0);
                    tenDaysAgo.set(Calendar.SECOND, 0);

                    Calendar sevenDaysAgo = Calendar.getInstance();
                    sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -6);
                    sevenDaysAgo.set(Calendar.HOUR_OF_DAY, 0);
                    sevenDaysAgo.set(Calendar.MINUTE, 0);
                    sevenDaysAgo.set(Calendar.SECOND, 0);

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Task task = doc.toObject(Task.class);
                        task.setTaskId(doc.getId());
                        
                        // Fix Workspace ID extraction logic
                        String wsId = task.getWorkspaceId();
                        if (wsId == null || wsId.isEmpty()) {
                            try {
                                if (doc.getReference().getParent().getParent() != null) {
                                    wsId = doc.getReference().getParent().getParent().getId();
                                    task.setWorkspaceId(wsId);
                                }
                            } catch (Exception ignored) {}
                        }

                        Timestamp endTs = task.getEndDate();
                        if (endTs == null) continue;
                        
                        Date endDate = endTs.toDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endDate);

                        // Basic stats
                        if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                            if (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)) monthlyCount++;
                            if (cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)) weeklyCount++;
                        }

                        // Last 10 days for Pie Chart
                        if (!endDate.before(tenDaysAgo.getTime()) && Constants.TASK_STATUS_DONE.equals(task.getStatus())) {
                            long duration = 60; 
                            if (task.getStartDate() != null && task.getEndDate() != null) {
                                long diffMs = task.getEndDate().toDate().getTime() - task.getStartDate().toDate().getTime();
                                duration = TimeUnit.MILLISECONDS.toMinutes(diffMs);
                                if (duration <= 0) duration = 60;
                            }
                            
                            if (wsId != null) {
                                workspaceTimeMap.put(wsId, workspaceTimeMap.getOrDefault(wsId, 0L) + duration);
                            }
                            totalTimeMinutes += duration;
                        }

                        // Last 7 days for Bar/Line Chart
                        if (!endDate.before(sevenDaysAgo.getTime())) {
                            long diffMs = endDate.getTime() - sevenDaysAgo.getTimeInMillis();
                            int dayIdx = (int) (diffMs / (24 * 60 * 60 * 1000));
                            if (dayIdx >= 0 && dayIdx < 7) {
                                dailyTotal[dayIdx]++;
                                if (Constants.TASK_STATUS_DONE.equals(task.getStatus())) {
                                    dailyDone[dayIdx]++;
                                }
                            }
                        }
                    }

                    updateUI(weeklyCount, monthlyCount, totalTimeMinutes);
                    updatePieChart(workspaceTimeMap, totalTimeMinutes);
                    updateBarChart(dailyDone);
                    updateLineChart(dailyTotal, dailyDone);
                });
    }

    private void updateUI(int weekly, int monthly, long time) {
        tvWeeklyTasks.setText(String.valueOf(weekly));
        tvMonthlyTasks.setText(String.valueOf(monthly));
        tvTotalWorkTime.setText((time / 60) + "h " + (time % 60) + "m");
    }

    private void updatePieChart(Map<String, Long> timeMap, long totalMinutes) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : timeMap.entrySet()) {
            String wsName = workspaceNameMap.getOrDefault(entry.getKey(), "Khác");
            entries.add(new PieEntry(entry.getValue(), wsName));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có task hoàn thành 10 ngày qua");
            pieChart.setNoDataTextColor(Color.GRAY);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
            Color.parseColor("#4285F4"), Color.parseColor("#EA4335"), 
            Color.parseColor("#FBBC05"), Color.parseColor("#34A853"), 
            Color.parseColor("#9C27B0"), Color.parseColor("#00ACC1"),
            Color.parseColor("#FF7043"), Color.parseColor("#26A69A")
        });
        dataSet.setSliceSpace(4f);
        
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.3f);
        dataSet.setValueLineColor(Color.LTGRAY);
        
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 60) {
                    return (int)(value / 60) + "h " + (int)(value % 60) + "m";
                }
                return (int)value + "m";
            }
        });

        pieChart.setData(new PieData(dataSet));
        pieChart.setCenterText("TỔNG THỜI GIAN\n" + (totalMinutes / 60) + "h " + (totalMinutes % 60) + "m");
        pieChart.invalidate();
    }

    private void updateBarChart(int[] stats) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, stats[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số việc xong");
        dataSet.setColor(Color.parseColor("#4285F4"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawValues(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getRecentDaysLabels()));
        barChart.setData(new BarData(dataSet));
        barChart.invalidate();
    }

    private void updateLineChart(int[] total, int[] done) {
        ArrayList<Entry> totalEntries = new ArrayList<>();
        ArrayList<Entry> doneEntries = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            totalEntries.add(new Entry(i, total[i]));
            doneEntries.add(new Entry(i, done[i]));
        }

        LineDataSet setTotal = new LineDataSet(totalEntries, "Dự kiến");
        setTotal.setColor(Color.GRAY);
        setTotal.setCircleColor(Color.GRAY);
        setTotal.setLineWidth(2f);
        setTotal.setDrawValues(false);
        setTotal.setMode(LineDataSet.Mode.LINEAR); // Linear mode to avoid negative curves

        LineDataSet setDone = new LineDataSet(doneEntries, "Thực tế");
        setDone.setColor(Color.parseColor("#59D595"));
        setDone.setCircleColor(Color.parseColor("#59D595"));
        setDone.setLineWidth(3f);
        setDone.setDrawValues(false);
        setDone.setMode(LineDataSet.Mode.LINEAR);

        lineChart.setData(new LineData(setTotal, setDone));
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getRecentDaysLabels()));
        lineChart.invalidate();
    }

    private String[] getRecentDaysLabels() {
        String[] days = new String[7];
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            days[i] = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
