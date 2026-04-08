package com.tngoc.familytaskapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.BaseActivity;
import com.tngoc.familytaskapp.ui.home.HomeActivity;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If user is already logged in, go to HomeActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnLogin).setOnClickListener(v ->
            startActivity(new Intent(this, LoginActivity.class))
        );

        findViewById(R.id.btnRegister).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
