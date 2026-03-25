package com.tngoc.familytaskapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.tngoc.familytaskapp.MainActivity;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.UserRepository;
import com.tngoc.familytaskapp.ui.auth.AuthViewModel;
import com.tngoc.familytaskapp.ui.auth.LoginActivity;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout llNormalHeader;
    private LinearLayout llAddWorkspaceHeader;
    private LinearLayout llWorkspaceContainer;
    private EditText etNewWorkspaceName;
    private EditText etSearch;
    private TextView tvWelcome;
    
    private WorkspaceViewModel workspaceViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private String currentUserId;
    private String currentUserName;
    
    private List<Workspace> allWorkspaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ViewModels and Repositories
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository();

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        
        // Initialize views
        llNormalHeader = findViewById(R.id.llNormalHeader);
        llAddWorkspaceHeader = findViewById(R.id.llAddWorkspaceHeader);
        llWorkspaceContainer = findViewById(R.id.llWorkspaceContainer);
        etNewWorkspaceName = findViewById(R.id.etNewWorkspaceName);
        etSearch = findViewById(R.id.etSearch);
        tvWelcome = findViewById(R.id.tvWelcome);
        
        // Fetch current user name and update welcome message
        MutableLiveData<String> nameLiveData = new MutableLiveData<>();
        nameLiveData.observe(this, name -> {
            if (name != null) {
                currentUserName = capitalizeAndFormat(name);
                if (tvWelcome != null) {
                    tvWelcome.setText("Xin chào " + currentUserName);
                }
            }
        });
        userRepository.getUserName(currentUserId, nameLiveData);

        Button btnAddWorkspace = findViewById(R.id.btnAddWorkspace);
        Button btnCancelAdd = findViewById(R.id.btnCancelAdd);
        Button btnSubmitAdd = findViewById(R.id.btnSubmitAdd);

        // Set up BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.homeFragment);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                llNormalHeader.setVisibility(View.VISIBLE);
                llAddWorkspaceHeader.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.taskFragment) {
                if (!allWorkspaces.isEmpty()) {
                    // Mở danh sách task của workspace đầu tiên
                    Workspace firstWs = allWorkspaces.get(0);
                    openTaskList(firstWs.getWorkspaceId(), firstWs.getName());
                } else {
                    Toast.makeText(this, "Bạn chưa có workspace nào để xem nhiệm vụ", Toast.LENGTH_SHORT).show();
                    bottomNav.setSelectedItemId(R.id.homeFragment);
                }
                return true;
            } else if (itemId == R.id.chatBotFragment) Toast.makeText(this, "Chat bot", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.notificationFragment) Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.profileFragment) Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Search logic
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterWorkspaces(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddWorkspace != null) {
            btnAddWorkspace.setOnClickListener(v -> {
                llNormalHeader.setVisibility(View.GONE);
                llAddWorkspaceHeader.setVisibility(View.VISIBLE);
            });
        }

        if (btnCancelAdd != null) {
            btnCancelAdd.setOnClickListener(v -> {
                llNormalHeader.setVisibility(View.VISIBLE);
                llAddWorkspaceHeader.setVisibility(View.GONE);
            });
        }

        if (btnSubmitAdd != null) {
            btnSubmitAdd.setOnClickListener(v -> {
                String name = etNewWorkspaceName.getText().toString().trim();
                if (!name.isEmpty()) {
                    List<String> members = new ArrayList<>();
                    members.add(currentUserId);
                    String creatorName = (currentUserName != null) ? currentUserName : "Người dùng";
                    Workspace newWs = new Workspace(null, name, "", currentUserId, creatorName, members);
                    workspaceViewModel.createWorkspace(newWs);
                    llNormalHeader.setVisibility(View.VISIBLE);
                    llAddWorkspaceHeader.setVisibility(View.GONE);
                    etNewWorkspaceName.setText("");
                }
            });
        }

        workspaceViewModel.workspacesLiveData.observe(this, workspaces -> {
            if (workspaces != null) {
                allWorkspaces = workspaces;
                filterWorkspaces(etSearch.getText().toString());
            }
        });

        workspaceViewModel.workspaceIdLiveData.observe(this, id -> {
            if (id != null) workspaceViewModel.loadWorkspacesForUser(currentUserId);
        });

        workspaceViewModel.loadWorkspacesForUser(currentUserId);
    }

    public void logout() {
        authViewModel.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void openTaskList(String workspaceId, String workspaceName) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("OPEN_TASK_LIST", true);
        intent.putExtra("workspaceId", workspaceId);
        intent.putExtra("workspaceName", workspaceName);
        startActivity(intent);
    }

    private String capitalizeAndFormat(String name) {
        if (name == null || name.isEmpty()) return name;
        String[] words = name.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void filterWorkspaces(String query) {
        llWorkspaceContainer.removeAllViews();
        String lowerCaseQuery = query.toLowerCase().trim();
        for (Workspace ws : allWorkspaces) {
            if (ws.getName().toLowerCase().contains(lowerCaseQuery)) {
                addWorkspaceCardToUI(ws);
            }
        }
    }

    private void addWorkspaceCardToUI(Workspace workspace) {
        View workspaceView = LayoutInflater.from(this).inflate(R.layout.item_workspace, llWorkspaceContainer, false);
        ((TextView) workspaceView.findViewById(R.id.tvWorkspaceName)).setText(workspace.getName());
        ((TextView) workspaceView.findViewById(R.id.tvOwnerName)).setText("Tạo bởi: " + (workspace.getOwnerName() != null ? workspace.getOwnerName() : "Unknown"));
        
        int total = workspace.getTotalTasks();
        int completed = workspace.getCompletedTasks();
        ((TextView) workspaceView.findViewById(R.id.tvTaskCount)).setText(completed + "/" + total + " task");
        
        ProgressBar pb = workspaceView.findViewById(R.id.pbWorkspace);
        if (total > 0) pb.setProgress((int) (((float) completed / total) * 100));
        else pb.setProgress(0);

        workspaceView.setOnClickListener(v -> openTaskList(workspace.getWorkspaceId(), workspace.getName()));
        llWorkspaceContainer.addView(workspaceView);
    }
}
