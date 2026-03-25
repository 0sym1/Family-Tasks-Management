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
                // Reset to initial state
                llNormalHeader.setVisibility(View.VISIBLE);
                llAddWorkspaceHeader.setVisibility(View.GONE);
                etNewWorkspaceName.setText("");
                return true;
            } else if (itemId == R.id.taskFragment) Toast.makeText(this, "Nhiệm vụ", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.chatBotFragment) Toast.makeText(this, "Chat bot", Toast.LENGTH_SHORT).show();
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

            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    filterWorkspaces(etSearch.getText().toString());
                    return true;
                }
                return false;
            });
        }

        // Switch to Add Workspace view
        if (btnAddWorkspace != null) {
            btnAddWorkspace.setOnClickListener(v -> {
                llNormalHeader.setVisibility(View.GONE);
                llAddWorkspaceHeader.setVisibility(View.VISIBLE);
            });
        }

        // Switch back to Normal view
        if (btnCancelAdd != null) {
            btnCancelAdd.setOnClickListener(v -> {
                llNormalHeader.setVisibility(View.VISIBLE);
                llAddWorkspaceHeader.setVisibility(View.GONE);
                etNewWorkspaceName.setText("");
            });
        }

        // Add new workspace logic
        if (btnSubmitAdd != null) {
            btnSubmitAdd.setOnClickListener(v -> {
                String name = etNewWorkspaceName.getText().toString().trim();
                if (!name.isEmpty()) {
                    List<String> members = new ArrayList<>();
                    members.add(currentUserId);
                    // Use current user's name if available, otherwise fallback
                    String creatorName = (currentUserName != null) ? currentUserName : "Người dùng";
                    Workspace newWs = new Workspace(null, name, "", currentUserId, creatorName, members);
                    workspaceViewModel.createWorkspace(newWs);
                    
                    llNormalHeader.setVisibility(View.VISIBLE);
                    llAddWorkspaceHeader.setVisibility(View.GONE);
                    etNewWorkspaceName.setText("");
                } else {
                    Toast.makeText(this, "Vui lòng nhập tên workspace", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Observe Workspaces
        workspaceViewModel.workspacesLiveData.observe(this, workspaces -> {
            if (workspaces != null) {
                allWorkspaces = workspaces;
                filterWorkspaces(etSearch.getText().toString());
            }
        });

        workspaceViewModel.workspaceIdLiveData.observe(this, id -> {
            if (id != null) {
                Toast.makeText(this, "Đã thêm workspace thành công", Toast.LENGTH_SHORT).show();
                workspaceViewModel.loadWorkspacesForUser(currentUserId);
            }
        });

        workspaceViewModel.successLiveData.observe(this, success -> {
            if (success != null && success) {
                workspaceViewModel.loadWorkspacesForUser(currentUserId);
            }
        });

        workspaceViewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load data
        workspaceViewModel.loadWorkspacesForUser(currentUserId);
    }

    private String capitalizeAndFormat(String name) {
        if (name == null || name.isEmpty()) return name;
        
        String[] words = name.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
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
        
        TextView tvName = workspaceView.findViewById(R.id.tvWorkspaceName);
        TextView tvOwner = workspaceView.findViewById(R.id.tvOwnerName);
        TextView tvTaskCount = workspaceView.findViewById(R.id.tvTaskCount);
        ProgressBar pb = workspaceView.findViewById(R.id.pbWorkspace);
        TextView tvOptions = workspaceView.findViewById(R.id.tvOptions);
        Button btnDelete = workspaceView.findViewById(R.id.btnDelete);
        
        tvName.setText(workspace.getName());
        tvOwner.setText("Tạo bởi: " + (workspace.getOwnerName() != null ? workspace.getOwnerName() : "Unknown"));
        
        int total = workspace.getTotalTasks();
        int completed = workspace.getCompletedTasks();
        tvTaskCount.setText(completed + "/" + total + " task");
        
        // Cập nhật progress bar theo tỷ lệ hoàn thành (0-100)
        if (total > 0) {
            int progress = (int) (((float) completed / total) * 100);
            pb.setProgress(progress);
        } else {
            pb.setProgress(0);
        }
        
        tvOptions.setOnClickListener(v -> {
            btnDelete.setVisibility(btnDelete.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        
        btnDelete.setOnClickListener(v -> {
            workspaceViewModel.deleteWorkspace(workspace.getWorkspaceId());
            Toast.makeText(this, "Đang xóa workspace...", Toast.LENGTH_SHORT).show();
        });

        // Add Click listener to open TaskListFragment
        workspaceView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_TASK_LIST", true);
                intent.putExtra("workspaceId", workspace.getWorkspaceId());
                intent.putExtra("workspaceName", workspace.getName());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Chuyển sang danh sách nhiệm vụ...", Toast.LENGTH_SHORT).show();
            }
        });

        llWorkspaceContainer.addView(workspaceView);
    }

    public void logout() {
        authViewModel.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
