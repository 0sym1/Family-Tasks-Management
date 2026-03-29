package com.tngoc.familytaskapp.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private String workspaceId;

    private Button btnTabAll, btnTabTask, btnTabPoint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
        }
        
        // Nếu không có workspaceId từ args, có thể thử lấy cái mặc định hoặc báo lỗi
        // Ở đây tạm thời để test nếu null
        if (workspaceId == null) {
            workspaceId = "default_workspace"; // Thay bằng logic thực tế của bạn
        }

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupTabs();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        btnTabAll = view.findViewById(R.id.btnTabAll);
        btnTabTask = view.findViewById(R.id.btnTabTask);
        btnTabPoint = view.findViewById(R.id.btnTabPoint);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        historyViewModel.historyLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setData(items);
            }
        });

        historyViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        historyViewModel.loadAllData(workspaceId);
    }

    private void setupTabs() {
        btnTabAll.setOnClickListener(v -> {
            updateTabUI(btnTabAll);
            historyViewModel.filterAll();
        });

        btnTabTask.setOnClickListener(v -> {
            updateTabUI(btnTabTask);
            historyViewModel.filterTasks();
        });

        btnTabPoint.setOnClickListener(v -> {
            updateTabUI(btnTabPoint);
            historyViewModel.filterPoints();
        });
        
        // Mặc định chọn Tab Tất cả
        updateTabUI(btnTabAll);
    }

    private void updateTabUI(Button selectedTab) {
        // Reset tất cả tab về style chưa chọn
        resetTabStyle(btnTabAll);
        resetTabStyle(btnTabTask);
        resetTabStyle(btnTabPoint);

        // Set style cho tab được chọn
        selectedTab.setBackgroundResource(R.drawable.bg_button_dark);
        selectedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetTabStyle(Button btn) {
        btn.setBackgroundResource(android.R.color.transparent);
        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }
}
