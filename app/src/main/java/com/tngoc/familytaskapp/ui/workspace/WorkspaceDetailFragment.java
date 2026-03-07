package com.tngoc.familytaskapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;

public class WorkspaceDetailFragment extends Fragment {

    private WorkspaceViewModel workspaceViewModel;
    private String workspaceId;
    private RecyclerView recyclerViewMembers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workspace_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        // Nhận workspaceId từ arguments
        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
        }

        recyclerViewMembers = view.findViewById(R.id.recyclerViewMembers);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.btnInviteMember).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_workspaceDetail_to_inviteMember)
        );

        view.findViewById(R.id.btnAddTask).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_workspaceDetail_to_createTask)
        );

        observeViewModel();
        if (workspaceId != null) workspaceViewModel.loadWorkspace(workspaceId);
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null) {
                // TODO: fill UI với data
            }
        });

        workspaceViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

