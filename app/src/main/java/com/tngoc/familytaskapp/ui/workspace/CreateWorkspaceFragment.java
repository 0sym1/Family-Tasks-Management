package com.tngoc.familytaskapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Workspace;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateWorkspaceFragment extends Fragment {

    private WorkspaceViewModel workspaceViewModel;
    private EditText etName, etDescription;
    private Button btnCreate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workspace, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        etName        = view.findViewById(R.id.etWorkspaceName);
        etDescription = view.findViewById(R.id.etWorkspaceDescription);
        btnCreate     = view.findViewById(R.id.btnCreateWorkspace);

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Workspace workspace = new Workspace(null, name, desc, uid, new ArrayList<>(Arrays.asList(uid)));
            workspaceViewModel.createWorkspace(workspace);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceIdLiveData.observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                Toast.makeText(requireContext(), getString(R.string.workspace_created), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        workspaceViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}

