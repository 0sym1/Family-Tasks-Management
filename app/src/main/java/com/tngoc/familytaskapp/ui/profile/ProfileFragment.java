package com.tngoc.familytaskapp.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private FrameLayout layoutAvatar;
    private TextView tvName, tvUsername, tvPoints, tvEmail;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnLanguageVi, btnLanguageEn, btnSave, btnLogout;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Đăng ký Activity Result để chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadAvatar(imageUri);
                        }
                    }
                }
        );
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
        ivAvatar = view.findViewById(R.id.ivAvatar);
        layoutAvatar = view.findViewById(R.id.layoutAvatar);
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
    }

    private void setupListeners() {
        if (layoutAvatar != null) {
            layoutAvatar.setOnClickListener(v -> openImagePicker());
        }
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadAvatar(Uri imageUri) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            profileViewModel.uploadAvatar(currentUser.getUid(), imageUri);
        }
    }

    private void observeViewModel() {
        profileViewModel.userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                displayUserInfo(user);
            }
        });

        profileViewModel.avatarUrlLiveData.observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).circleCrop().into(ivAvatar);
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                clearPasswordFields();
            }
        });

        profileViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        profileViewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading -> {
            // Có thể thêm ProgressBar ở đây nếu muốn
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) return;

        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvName.setText(user.getDisplayName());
            tvUsername.setText("@" + user.getDisplayName().toLowerCase().replace(" ", ""));
        }

        tvPoints.setText(getString(R.string.profile_points, user.getPoints()));

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            tvEmail.setText(user.getEmail());
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_user_default)
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void saveProfile() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), "Tính năng đổi mật khẩu đang được cập nhật", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearPasswordFields() {
        if (etOldPassword != null) etOldPassword.setText("");
        if (etNewPassword != null) etNewPassword.setText("");
        if (etConfirmPassword != null) etConfirmPassword.setText("");
    }

    private void changeLanguage(String languageCode) {
        sharedPrefManager.saveLanguage(languageCode);
        LocaleHelper.setLocale(requireContext(), languageCode);
        requireActivity().recreate();
    }
}
