package com.tngoc.familytaskapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvPasswordError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Nếu đã đăng nhập → vào thẳng HomeActivity
        if (authViewModel.getCurrentUser() != null) {
            goToHome();
            return;
        }

        initViews();
        observeViewModel();
    }

    private void initViews() {
        etUsername     = findViewById(R.id.etUsername);
        etPassword     = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        progressBar    = findViewById(R.id.progressBar);

        AppCompatButton btnLogin  = findViewById(R.id.btnLogin);
        AppCompatButton btnCancel = findViewById(R.id.btnCancel);

        // Xóa lỗi khi user bắt đầu nhập lại
        etUsername.addTextChangedListener(clearError(tvUsernameError));
        etPassword.addTextChangedListener(clearError(tvPasswordError));

        btnLogin.setOnClickListener(v -> {
            String email    = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean valid = true;

            if (email.isEmpty()) {
                tvUsernameError.setText("Vui lòng nhập email");
                tvUsernameError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tvUsernameError.setText("Email không đúng định dạng");
                tvUsernameError.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (password.isEmpty()) {
                tvPasswordError.setText("Vui lòng nhập mật khẩu");
                tvPasswordError.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (!valid) return;
            authViewModel.login(email, password);
        });

        btnCancel.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }

    private void observeViewModel() {
        authViewModel.userLiveData.observe(this, user -> {
            if (user != null) goToHome();
        });

        authViewModel.errorLiveData.observe(this, error -> {
            if (error == null || error.isEmpty()) return;
            // Hiển thị lỗi từ Firebase vào đúng field
            if (error.contains("email") || error.contains("Email")) {
                tvUsernameError.setText(error);
                tvUsernameError.setVisibility(View.VISIBLE);
            } else if (error.contains("mật khẩu") || error.contains("password")) {
                tvPasswordError.setText(error);
                tvPasswordError.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.loadingLiveData.observe(this, isLoading ->
            progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );
    }

    private TextWatcher clearError(TextView tvError) {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
            }
            public void afterTextChanged(Editable s) {}
        };
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
