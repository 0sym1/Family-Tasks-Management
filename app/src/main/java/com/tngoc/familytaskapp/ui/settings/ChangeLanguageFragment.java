package com.tngoc.familytaskapp.ui.settings;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

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

        if (getContext() != null) {
            currentSavedLanguage = LocaleHelper.getSavedLanguage(requireContext(), "vi");
            selectedLanguage = currentSavedLanguage;
        }

        updateUI(view);
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        View btnVi = view.findViewById(R.id.btnLangVi);
        View btnEn = view.findViewById(R.id.btnLangEn);
        View btnFr = view.findViewById(R.id.btnLangFr);

        if (btnVi != null) {
            btnVi.setOnClickListener(v -> {
                selectedLanguage = "vi";
                updateUI(view);
            });
        }

        if (btnEn != null) {
            btnEn.setOnClickListener(v -> {
                selectedLanguage = "en";
                updateUI(view);
            });
        }

        if (btnFr != null) {
            btnFr.setOnClickListener(v -> {
                selectedLanguage = "fr";
                updateUI(view);
            });
        }

        View btnCancel = view.findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        View btnSave = view.findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveLanguageToFirebase(selectedLanguage));
        }
    }

    private void saveLanguageToFirebase(String langCode) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("language", langCode)
                    .addOnSuccessListener(aVoid -> applyLanguageAndRestart(langCode))
                    .addOnFailureListener(e -> applyLanguageAndRestart(langCode));
        } else {
            applyLanguageAndRestart(langCode);
        }
    }

    private void applyLanguageAndRestart(String langCode) {
        if (getContext() != null) {
            LocaleHelper.setLocale(requireContext(), langCode);
            restartApp();
        }
    }

    private void restartApp() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void updateUI(View root) {
        if (getContext() == null || root == null) return;
        
        int activeColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        updateRowUI(root, "vi", activeColor, inactiveColor);
        updateRowUI(root, "en", activeColor, inactiveColor);
        updateRowUI(root, "fr", activeColor, inactiveColor);
    }

    private void updateRowUI(View root, String langCode, int activeColor, int inactiveColor) {
        boolean isSelected = selectedLanguage.equals(langCode);
        boolean isActuallySaved = currentSavedLanguage.equals(langCode);

        RadioButton rb = root.findViewById(getRadioButtonId(langCode));
        TextView tv = root.findViewById(getTextViewId(langCode));
        ImageView ivCheck = root.findViewById(getCheckIconId(langCode));

        if (rb != null) {
            rb.setChecked(isSelected);
            rb.setButtonTintList(ColorStateList.valueOf(isSelected ? activeColor : inactiveColor));
        }
        if (tv != null) {
            tv.setTextColor(isSelected ? activeColor : inactiveColor);
        }
        if (ivCheck != null) {
            ivCheck.setVisibility(isActuallySaved ? View.VISIBLE : View.GONE);
        }
    }

    private int getRadioButtonId(String langCode) {
        if ("vi".equals(langCode)) return R.id.rbVi;
        if ("en".equals(langCode)) return R.id.rbEn;
        return R.id.rbFr;
    }

    private int getTextViewId(String langCode) {
        if ("vi".equals(langCode)) return R.id.tvVi;
        if ("en".equals(langCode)) return R.id.tvEn;
        return R.id.tvFr;
    }
    
    private int getCheckIconId(String langCode) {
        if ("vi".equals(langCode)) return R.id.ivCheckVi;
        if ("en".equals(langCode)) return R.id.ivCheckEn;
        return R.id.ivCheckFr;
    }
}