package com.tngoc.familytaskapp.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.auth.LoginActivity;
import com.tngoc.familytaskapp.ui.chatbot.ChatBotViewModel;

public class HomeActivity extends AppCompatActivity {

    private NavController navController;
    private ChatBotViewModel chatBotViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
    }

    public void logout() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
