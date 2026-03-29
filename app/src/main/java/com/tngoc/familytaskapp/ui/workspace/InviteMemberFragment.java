package com.tngoc.familytaskapp.ui.workspace;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.adapter.MemberAdapter;
import com.tngoc.familytaskapp.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InviteMemberFragment extends Fragment {

    private WorkspaceViewModel workspaceViewModel;
    private RecyclerView recyclerViewMembers;
    private MemberAdapter adapter;
    private TextView tvMemberCount;
    private EditText etSearchMember;
    private ImageButton btnBack;
    private String workspaceId;
    private List<User> allMembers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);

        if (getArguments() != null) {
            workspaceId = getArguments().getString("workspaceId");
        }

        btnBack = view.findViewById(R.id.btnBack);
        tvMemberCount = view.findViewById(R.id.tvMemberCount);
        etSearchMember = view.findViewById(R.id.etSearchMember);
        recyclerViewMembers = view.findViewById(R.id.recyclerViewMembers);

        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemberAdapter();
        recyclerViewMembers.setAdapter(adapter);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        setupSearch();
        observeViewModel();

        if (workspaceId != null) {
            workspaceViewModel.loadWorkspace(workspaceId);
        }
    }

    private void setupSearch() {
        etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMembers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : allMembers) {
            String name = (user.getDisplayName() != null ? user.getDisplayName() : user.getEmail()).toLowerCase();
            if (name.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.setMembers(filteredList);
        tvMemberCount.setText(filteredList.size() + " thành viên");
    }

    private void observeViewModel() {
        workspaceViewModel.workspaceLiveData.observe(getViewLifecycleOwner(), workspace -> {
            if (workspace != null && workspace.getMemberIds() != null) {
                workspaceViewModel.loadMembers(workspace.getMemberIds());
            }
        });

        workspaceViewModel.membersLiveData.observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                allMembers = new ArrayList<>(members);
                // Sắp xếp theo tên bảng chữ cái
                Collections.sort(allMembers, (u1, u2) -> {
                    String name1 = u1.getDisplayName() != null ? u1.getDisplayName() : u1.getEmail();
                    String name2 = u2.getDisplayName() != null ? u2.getDisplayName() : u2.getEmail();
                    return name1.compareToIgnoreCase(name2);
                });

                filterMembers(etSearchMember.getText().toString());
            }
        });
    }
}
