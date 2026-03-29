package com.tngoc.familytaskapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.ui.home.HomeActivity;
import com.tngoc.familytaskapp.utils.LocaleHelper;
import com.tngoc.familytaskapp.utils.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private SharedPrefManager sharedPrefManager;
    private FirebaseAuth firebaseAuth;

    private ImageView ivAvatar;
    private TextView tvName, tvUsername, tvPoints, tvEmail;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnLanguageVi, btnLanguageEn, btnSave, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sharedPrefManager = new SharedPrefManager(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            profileViewModel.loadUser(currentUser.getUid());
        }

        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        try {
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvName = view.findViewById(R.id.tvName);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvPoints = view.findViewById(R.id.tvPoints);
            tvEmail = view.findViewById(R.id.tvEmail);

            etOldPassword = view.findViewById(R.id.etOldPassword);
            etNewPassword = view.findViewById(R.id.etNewPassword);
            etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

            btnLanguageVi = view.findViewById(R.id.btnLanguageVi);
            btnLanguageEn = view.findViewById(R.id.btnLanguageEn);
            btnSave = view.findViewById(R.id.btnSave);
            btnLogout = view.findViewById(R.id.btnLogout);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        if (btnLanguageVi != null) {
            btnLanguageVi.setOnClickListener(v -> changeLanguage("vi"));
        }
        if (btnLanguageEn != null) {
            btnLanguageEn.setOnClickListener(v -> changeLanguage("en"));
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (requireActivity() instanceof HomeActivity) {
                    ((HomeActivity) requireActivity()).logout();
                }
            });
        }
    }

    private void observeViewModel() {
        profileViewModel.userLiveData.observe(getViewLifecycleOwner(), user -> {
            try {
                if (user != null) {
                    displayUserInfo(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error displaying user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                }
                clearPasswordFields();
            }
        });

        profileViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) {
            return;
        }

        // Display name
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvName.setText(user.getDisplayName());
            tvUsername.setText("@" + user.getDisplayName().toLowerCase().replace(" ", ""));
        }

        // Display points
        tvPoints.setText(getString(R.string.profile_points, user.getPoints()));

        // Display email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            tvEmail.setText(user.getEmail());
        }

        // Load avatar nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void saveProfile() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra nếu muốn đổi mật khẩu
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Update password through AuthRepository
            // Hiện tại chỉ lưu thông tin profile, mật khẩu cần được xử lý riêng
            Toast.makeText(requireContext(), "Tính năng đổi mật khẩu đang được cập nhật", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearPasswordFields() {
        try {
            if (etOldPassword != null) etOldPassword.setText("");
            if (etNewPassword != null) etNewPassword.setText("");
            if (etConfirmPassword != null) etConfirmPassword.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeLanguage(String languageCode) {
        sharedPrefManager.saveLanguage(languageCode);
        LocaleHelper.setLocale(requireContext(), languageCode);
        requireActivity().recreate();
    }
}

