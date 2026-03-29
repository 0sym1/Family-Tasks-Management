package com.tngoc.familytaskapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.home.HomeActivity;
import com.tngoc.familytaskapp.utils.LocaleHelper;

public class ChangeLanguageFragment extends Fragment {

    private String selectedLanguage = "vi";
    private String currentSavedLanguage = "vi";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get currently saved language to show existing tick
        currentSavedLanguage = LocaleHelper.getSavedLanguage(requireContext(), "vi");
        selectedLanguage = currentSavedLanguage;

        updateUI(view);

        view.findViewById(R.id.btnLangVi).setOnClickListener(v -> {
            selectedLanguage = "vi";
            updateUI(view);
        });

        view.findViewById(R.id.btnLangEn).setOnClickListener(v -> {
            selectedLanguage = "en";
            updateUI(view);
        });

        view.findViewById(R.id.btnLangFr).setOnClickListener(v -> {
            selectedLanguage = "fr";
            updateUI(view);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            saveLanguageToFirebase(selectedLanguage);
        });
    }

    private void saveLanguageToFirebase(String langCode) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("language", langCode)
                    .addOnSuccessListener(aVoid -> {
                        LocaleHelper.setLocale(requireContext(), langCode);
                        restartApp();
                    })
                    .addOnFailureListener(e -> {
                        // Even if firebase fails, we should apply it locally
                        LocaleHelper.setLocale(requireContext(), langCode);
                        restartApp();
                    });
        } else {
            LocaleHelper.setLocale(requireContext(), langCode);
            restartApp();
        }
    }

    private void restartApp() {
        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void updateUI(View root) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        updateRowUI(root.findViewById(R.id.btnLangVi), selectedLanguage.equals("vi"), currentSavedLanguage.equals("vi"), activeColor, inactiveColor);
        updateRowUI(root.findViewById(R.id.btnLangEn), selectedLanguage.equals("en"), currentSavedLanguage.equals("en"), activeColor, inactiveColor);
        updateRowUI(root.findViewById(R.id.btnLangFr), selectedLanguage.equals("fr"), currentSavedLanguage.equals("fr"), activeColor, inactiveColor);
    }

    private void updateRowUI(View row, boolean isSelected, boolean isActuallySaved, int activeColor, int inactiveColor) {
        RadioButton rb = (RadioButton) ((ViewGroup) row).getChildAt(0);
        TextView tv = (TextView) ((ViewGroup) row).getChildAt(2);
        ImageView iv = (ImageView) ((ViewGroup) row).getChildAt(3);

        rb.setChecked(isSelected);
        rb.setButtonTintList(android.content.res.ColorStateList.valueOf(isSelected ? activeColor : inactiveColor));
        tv.setTextColor(isSelected ? activeColor : inactiveColor);
        iv.setVisibility(isActuallySaved ? View.VISIBLE : View.GONE);
    }
}
