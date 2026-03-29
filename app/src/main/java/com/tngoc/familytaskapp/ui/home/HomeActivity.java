package com.tngoc.familytaskapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tngoc.familytaskapp.MainActivity;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.NotificationRepository;
import com.tngoc.familytaskapp.data.repository.UserRepository;
import com.tngoc.familytaskapp.ui.auth.AuthViewModel;
import com.tngoc.familytaskapp.ui.BaseActivity;
import com.tngoc.familytaskapp.ui.auth.LoginActivity;
import com.tngoc.familytaskapp.ui.auth.WelcomeActivity;
import com.tngoc.familytaskapp.ui.workspace.WorkspaceViewModel;
import com.tngoc.familytaskapp.ui.chatbot.ChatBotViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private LinearLayout llNormalHeader;
    private LinearLayout llAddWorkspaceHeader;
    private LinearLayout llWorkspaceContainer;
    private EditText etNewWorkspaceName;
    private EditText etSearch;
    private TextView tvWelcome;
    private FrameLayout fragmentContainer;
    
    private WorkspaceViewModel workspaceViewModel;
    private AuthViewModel authViewModel;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private String currentUserId;
    private String currentUserName;
    private BadgeDrawable notificationBadge;
    
    private List<Workspace> allWorkspaces = new ArrayList<>();
    private NavController navController;
    private ChatBotViewModel chatBotViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        notificationRepository = new NotificationRepository();
        userRepository = new UserRepository();

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        // Khởi tạo ChatBotViewModel ở cấp Activity để có thể chia sẻ với Fragment
        chatBotViewModel = new ViewModelProvider(this).get(ChatBotViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Xử lý sự kiện khi nhấn lại (re-select) vào item trên Bottom Navigation
            bottomNav.setOnItemReselectedListener(item -> {
                if (item.getItemId() == R.id.chatBotFragment) {
                    // Nếu đang ở ChatBotFragment và nhấn lại vào icon, reset cuộc hội thoại
                    chatBotViewModel.resetChat();
                }
            });
        }
        currentUserId = currentUser.getUid();
        
        llNormalHeader = findViewById(R.id.llNormalHeader);
        llAddWorkspaceHeader = findViewById(R.id.llAddWorkspaceHeader);
        llWorkspaceContainer = findViewById(R.id.llWorkspaceContainer);
        etNewWorkspaceName = findViewById(R.id.etNewWorkspaceName);
        etSearch = findViewById(R.id.etSearch);
        tvWelcome = findViewById(R.id.tvWelcome);
        fragmentContainer = findViewById(R.id.fragment_container);
        
        setupBottomNavigation();
        setupSearch();
        setupAddWorkspace();
        setupNotifications();

        MutableLiveData<String> nameLiveData = new MutableLiveData<>();
        nameLiveData.observe(this, name -> {
            if (name != null) {
                currentUserName = capitalizeAndFormat(name);
                if (tvWelcome != null) tvWelcome.setText("Xin chào " + currentUserName);
            }
        });
        userRepository.getUserName(currentUserId, nameLiveData);

        workspaceViewModel.workspacesLiveData.observe(this, workspaces -> {
            if (workspaces != null) {
                allWorkspaces = workspaces;
                filterWorkspaces(etSearch.getText().toString());
            }
        });

        workspaceViewModel.loadWorkspacesForUser(currentUserId);
    }

    public void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Navigate to Welcome screen
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        notificationBadge = bottomNav.getOrCreateBadge(R.id.notificationFragment);
        notificationBadge.setVisible(false);
        notificationBadge.setMaxCharacterCount(2); // 9+ style

        bottomNav.setSelectedItemId(R.id.homeFragment);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                showHomeContent();
                return true;
            } else if (itemId == R.id.notificationFragment) {
                showNotificationFragment();
                return true;
            } else if (itemId == R.id.profileFragment) {
                showProfileFragment();
                return true;
            } else if (itemId == R.id.taskFragment) {
                if (!allWorkspaces.isEmpty()) {
                    openTaskList(allWorkspaces.get(0).getWorkspaceId(), allWorkspaces.get(0).getName());
                } else {
                    Toast.makeText(this, "Bạn chưa có workspace nào", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            return false;
        });
    }

    private void setupNotifications() {
        MutableLiveData<Integer> unreadCount = new MutableLiveData<>();
        unreadCount.observe(this, count -> {
            if (count != null && count > 0) {
                notificationBadge.setVisible(true);
                notificationBadge.setNumber(count);
            } else {
                notificationBadge.setVisible(false);
            }
        });
        notificationRepository.getUnreadCountRealtime(currentUserId, unreadCount);
    }

    private void showHomeContent() {
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        findViewById(R.id.mainScrollView).setVisibility(View.VISIBLE);
    }

    private void showNotificationFragment() {
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.tngoc.familytaskapp.ui.notification.NotificationFragment())
                    .commit();
            notificationRepository.markAllAsRead(currentUserId);
        }
    }

    private void showProfileFragment() {
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.tngoc.familytaskapp.ui.profile.ProfileFragment())
                    .commit();
        }
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterWorkspaces(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupAddWorkspace() {
        findViewById(R.id.btnAddWorkspace).setOnClickListener(v -> {
            llNormalHeader.setVisibility(View.GONE);
            llAddWorkspaceHeader.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.btnCancelAdd).setOnClickListener(v -> {
            llNormalHeader.setVisibility(View.VISIBLE);
            llAddWorkspaceHeader.setVisibility(View.GONE);
        });
        findViewById(R.id.btnSubmitAdd).setOnClickListener(v -> {
            String name = etNewWorkspaceName.getText().toString().trim();
            if (!name.isEmpty()) {
                List<String> members = new ArrayList<>();
                members.add(currentUserId);
                workspaceViewModel.createWorkspace(new Workspace(null, name, "", currentUserId, currentUserName, members));
                llNormalHeader.setVisibility(View.VISIBLE);
                llAddWorkspaceHeader.setVisibility(View.GONE);
                etNewWorkspaceName.setText("");
            }
        });
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

        TextView tvOptions = workspaceView.findViewById(R.id.tvOptions);
        Button btnDelete = workspaceView.findViewById(R.id.btnDelete);

        tvOptions.setOnClickListener(v -> btnDelete.setVisibility(btnDelete.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa Workspace")
                    .setMessage("Bạn có chắc chắn muốn xóa \"" + workspace.getName() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> workspaceViewModel.deleteWorkspace(workspace.getWorkspaceId()))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        workspaceView.setOnClickListener(v -> openTaskList(workspace.getWorkspaceId(), workspace.getName()));
        llWorkspaceContainer.addView(workspaceView);
    }
}
