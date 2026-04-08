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

import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.BaseActivity;
import com.tngoc.familytaskapp.ui.home.HomeActivity;

public class RegisterActivity extends BaseActivity {

    private AuthViewModel authViewModel;
    private EditText etName, etEmail, etUsername, etPassword, etConfirmPassword;
    private TextView tvNameError, tvEmailError, tvUsernameError, tvPasswordError, tvConfirmError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        initViews();
        observeViewModel();
    }

    private void initViews() {
        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etEmail);
        etUsername        = findViewById(R.id.etUsername);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvNameError       = findViewById(R.id.tvNameError);
        tvEmailError      = findViewById(R.id.tvEmailError);
        tvUsernameError   = findViewById(R.id.tvUsernameError);
        tvPasswordError   = findViewById(R.id.tvPasswordError);
        tvConfirmError    = findViewById(R.id.tvConfirmError);
        progressBar       = findViewById(R.id.progressBar);

        AppCompatButton btnRegister = findViewById(R.id.btnRegister);
        AppCompatButton btnCancel   = findViewById(R.id.btnCancel);

        // Xóa lỗi khi user nhập lại
        etName.addTextChangedListener(clearError(tvNameError));
        etEmail.addTextChangedListener(clearError(tvEmailError));
        etUsername.addTextChangedListener(clearError(tvUsernameError));
        etPassword.addTextChangedListener(clearError(tvPasswordError));
        etConfirmPassword.addTextChangedListener(clearError(tvConfirmError));

        btnRegister.setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etConfirmPassword.getText().toString().trim();

            boolean valid = true;

            // Validate tên
            if (name.isEmpty()) {
                tvNameError.setText("Vui lòng nhập tên của bạn");
                tvNameError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (name.length() < 2) {
                tvNameError.setText("Tên phải có ít nhất 2 ký tự");
                tvNameError.setVisibility(View.VISIBLE);
                valid = false;
            }

            // Validate email
            if (email.isEmpty()) {
                tvEmailError.setText("Vui lòng nhập email");
                tvEmailError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tvEmailError.setText("Email không đúng định dạng");
                tvEmailError.setVisibility(View.VISIBLE);
                valid = false;
            }

            // Validate username
            if (username.isEmpty()) {
                tvUsernameError.setText("Vui lòng nhập tên đăng nhập");
                tvUsernameError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (username.length() < 3) {
                tvUsernameError.setText("Tên đăng nhập phải có ít nhất 3 ký tự");
                tvUsernameError.setVisibility(View.VISIBLE);
                valid = false;
            }

            // Validate mật khẩu
            if (password.isEmpty()) {
                tvPasswordError.setText("Vui lòng nhập mật khẩu");
                tvPasswordError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (password.length() < 6) {
                tvPasswordError.setText("Mật khẩu phải có ít nhất 6 ký tự");
                tvPasswordError.setVisibility(View.VISIBLE);
                valid = false;
            }

            // Validate xác nhận mật khẩu
            if (confirm.isEmpty()) {
                tvConfirmError.setText("Vui lòng xác nhận mật khẩu");
                tvConfirmError.setVisibility(View.VISIBLE);
                valid = false;
            } else if (!password.equals(confirm)) {
                tvConfirmError.setText("Mật khẩu xác nhận không khớp");
                tvConfirmError.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (!valid) return;
            authViewModel.register(email, password, name, username);
        });

        btnCancel.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }

    private void observeViewModel() {
        authViewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        authViewModel.errorLiveData.observe(this, error -> {
            if (error == null || error.isEmpty()) return;
            // Phân loại lỗi Firebase vào đúng field
            if (error.contains("Email đã được sử dụng") || error.contains("email")) {
                tvEmailError.setText(error);
                tvEmailError.setVisibility(View.VISIBLE);
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
}
