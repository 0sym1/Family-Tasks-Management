package com.tngoc.familytaskapp.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private String workspaceId;

    private Button btnTabAll, btnTabTask, btnTabPoint;
    private EditText etSearchHistory;

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
        
        if (workspaceId == null) {
            workspaceId = "default_workspace"; 
        }

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupTabs();
        setupSearch();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        btnTabAll = view.findViewById(R.id.btnTabAll);
        btnTabTask = view.findViewById(R.id.btnTabTask);
        btnTabPoint = view.findViewById(R.id.btnTabPoint);
        etSearchHistory = view.findViewById(R.id.etSearchHistory);
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
        
        updateTabUI(btnTabAll);
    }

    private void setupSearch() {
        etSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                historyViewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateTabUI(Button selectedTab) {
        resetTabStyle(btnTabAll);
        resetTabStyle(btnTabTask);
        resetTabStyle(btnTabPoint);

        selectedTab.setBackgroundResource(R.drawable.bg_button_dark);
        selectedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetTabStyle(Button btn) {
        btn.setBackgroundResource(android.R.color.transparent);
        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }
}
