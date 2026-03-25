package com.tngoc.familytaskapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.auth.LoginActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Set home as default active item
        bottomNav.setSelectedItemId(R.id.homeFragment);
        
        // Handle navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.homeFragment) {
                // Home - already here
                return true;
            } else if (item.getItemId() == R.id.taskFragment) {
                Toast.makeText(this, "Nhiệm vụ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.chatBotFragment) {
                Toast.makeText(this, "Chat bot", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.notificationFragment) {
                Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.profileFragment) {
                Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    public void logout() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}



