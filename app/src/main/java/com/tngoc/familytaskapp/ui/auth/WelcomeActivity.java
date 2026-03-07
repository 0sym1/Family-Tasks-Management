package com.tngoc.familytaskapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tngoc.familytaskapp.R;

public class WelcomeActivity extends AppCompatActivity {

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

