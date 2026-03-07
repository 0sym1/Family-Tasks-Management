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

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;

public class WorkspaceListFragment extends Fragment {

    private WorkspaceViewModel workspaceViewModel;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workspace_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerViewWorkspaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.fabAddWorkspace).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_workspaceList_to_createWorkspace)
        );

        observeViewModel();
        loadData();
    }

    private void loadData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) workspaceViewModel.loadWorkspacesForUser(uid);
    }

    private void observeViewModel() {
        workspaceViewModel.workspacesLiveData.observe(getViewLifecycleOwner(), workspaces -> {
            // TODO: set adapter data
        });

        workspaceViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

