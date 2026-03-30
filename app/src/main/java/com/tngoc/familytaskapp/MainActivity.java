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
        boolean showTaskDetail = intent.getBooleanExtra("SHOW_TASK_DETAIL", false);
        boolean openWorkReport = intent.getBooleanExtra("OPEN_WORK_REPORT", false);

        if (!openTaskList && !showTaskDetail && !openWorkReport) {
            // Luồng khởi tạo bình thường (Splash/Redirect)
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                startActivity(new Intent(this, WelcomeActivity.class));
            }
            finish();
            return;
        }

        // Luồng mở từ HomeActivity
        setContentView(R.layout.activity_main);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            Bundle bundle = new Bundle();
            
            if (showTaskDetail) {
                bundle.putString("workspaceId", intent.getStringExtra("workspaceId"));
                bundle.putString("taskId", intent.getStringExtra("taskId"));
                navController.navigate(R.id.taskDetailFragment, bundle);
            } else if (openWorkReport) {
                navController.navigate(R.id.workReportFragment);
            } else {
                bundle.putString("workspaceId", intent.getStringExtra("workspaceId"));
                bundle.putString("workspaceName", intent.getStringExtra("workspaceName"));
                navController.navigate(R.id.taskListFragment, bundle);
            }
        }
    }
}
