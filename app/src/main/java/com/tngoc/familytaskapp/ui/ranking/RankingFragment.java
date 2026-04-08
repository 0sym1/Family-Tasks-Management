package com.tngoc.familytaskapp.ui.ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.BaseFragment;

public class RankingFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private RankingAdapter adapter;
    private RankingViewModel viewModel;
    private String workspaceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ranking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
        }

        initViews(view);
        setupViewModel();
        
        if (workspaceId != null) {
            viewModel.loadRanking(workspaceId);
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy Workspace ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewRanking);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new RankingAdapter();
        recyclerView.setAdapter(adapter);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RankingViewModel.class);
        
        viewModel.rankingLiveData.observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                adapter.setUsers(users);
            }
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), this::showLoading);
    }
}