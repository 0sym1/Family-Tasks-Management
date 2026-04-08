package com.tngoc.familytaskapp.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.ui.BaseFragment;
import com.tngoc.familytaskapp.ui.home.HomeActivity;
import com.tngoc.familytaskapp.ui.settings.ChangeLanguageFragment;
import com.tngoc.familytaskapp.utils.SharedPrefManager;

public class ProfileFragment extends BaseFragment {

    private ProfileViewModel profileViewModel;
    private SharedPrefManager sharedPrefManager;
    private FirebaseAuth firebaseAuth;

    private ImageView ivAvatar;
    private FrameLayout layoutAvatar;
    private TextView tvName, tvUsername, tvPoints, tvEmail, tvCurrentLanguage;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSave, btnLogout;
    private View llChangeLanguage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleLocalAvatar(imageUri);
                        }
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(requireContext(), "Cần quyền truy cập ảnh để thay đổi avatar", Toast.LENGTH_SHORT).show();
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
        updateLanguageDisplay();
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

        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        llChangeLanguage = view.findViewById(R.id.llChangeLanguage);
        tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage);
    }

    private void setupListeners() {
        if (layoutAvatar != null) {
            layoutAvatar.setOnClickListener(v -> requestImagePermissionAndPick());
        }

        if (llChangeLanguage != null) {
            llChangeLanguage.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_profile_to_changeLanguage);
                } catch (Exception e) {
                    if (isAdded()) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ChangeLanguageFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });
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

    private void updateLanguageDisplay() {
        String langCode = sharedPrefManager.getLanguage();
        if (tvCurrentLanguage != null) {
            if ("vi".equals(langCode)) {
                tvCurrentLanguage.setText("Tiếng Việt");
            } else if ("en".equals(langCode)) {
                tvCurrentLanguage.setText("English");
            } else if ("fr".equals(langCode)) {
                tvCurrentLanguage.setText("Français");
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void requestImagePermissionAndPick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void handleLocalAvatar(Uri imageUri) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Lưu Uri ảnh vào Shared Preferences cục bộ
            sharedPrefManager.saveUserAvatar(userId, imageUri.toString());
            
            // Hiển thị ngay lên ImageView
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(ivAvatar);
            
            Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện cục bộ", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        profileViewModel.userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                displayUserInfo(user);
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
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        profileViewModel.loadingLiveData.observe(getViewLifecycleOwner(), this::showLoading);
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

        // Ưu tiên hiển thị ảnh từ Local trước, nếu không có mới dùng ảnh từ Firebase (Cloud)
        String localAvatar = sharedPrefManager.getUserAvatar(user.getUserId());
        Object imageSource = (localAvatar != null) ? Uri.parse(localAvatar) : user.getAvatarUrl();

        Glide.with(this)
                .load(imageSource)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .circleCrop()
                .into(ivAvatar);
    }

    private void saveProfile() {
        Toast.makeText(requireContext(), "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
    }

    private void clearPasswordFields() {
        if (etOldPassword != null) etOldPassword.setText("");
        if (etNewPassword != null) etNewPassword.setText("");
        if (etConfirmPassword != null) etConfirmPassword.setText("");
    }
}