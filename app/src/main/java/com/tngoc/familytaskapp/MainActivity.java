package com.tngoc.familytaskapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.ui.BaseActivity;
import com.tngoc.familytaskapp.ui.auth.WelcomeActivity;
import com.tngoc.familytaskapp.ui.home.HomeActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean openTaskList = intent.getBooleanExtra("OPEN_TASK_LIST", false);

        if (!openTaskList) {
            // Luồng khởi tạo bình thường (Splash/Redirect)
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                startActivity(new Intent(this, WelcomeActivity.class));
            }
            finish();
            return;
        }

        // Luồng mở danh sách task từ HomeActivity
        setContentView(R.layout.activity_main);
        
        String wsId = intent.getStringExtra("workspaceId");
        String wsName = intent.getStringExtra("workspaceName");

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            Bundle bundle = new Bundle();
            bundle.putString("workspaceId", wsId);
            bundle.putString("workspaceName", wsName);
            
            // Navigate đến taskListFragment
            navController.navigate(R.id.taskListFragment, bundle);
        }
    }
}
