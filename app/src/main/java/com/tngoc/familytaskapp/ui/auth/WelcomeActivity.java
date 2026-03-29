package com.tngoc.familytaskapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.BaseActivity;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnLogin).setOnClickListener(v ->
            startActivity(new Intent(this, LoginActivity.class))
        );

        findViewById(R.id.btnRegister).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
