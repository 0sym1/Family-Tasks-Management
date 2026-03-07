package com.tngoc.familytaskapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tngoc.familytaskapp.R;
import com.tngoc.familytaskapp.ui.home.HomeActivity;
import com.tngoc.familytaskapp.utils.LocaleHelper;
import com.tngoc.familytaskapp.utils.SharedPrefManager;

public class SettingsFragment extends Fragment {

    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPrefManager = new SharedPrefManager(requireContext());

        view.findViewById(R.id.btnLanguageVi).setOnClickListener(v -> changeLanguage("vi"));
        view.findViewById(R.id.btnLanguageEn).setOnClickListener(v -> changeLanguage("en"));

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (requireActivity() instanceof HomeActivity) {
                ((HomeActivity) requireActivity()).logout();
            }
        });
    }

    private void changeLanguage(String languageCode) {
        sharedPrefManager.saveLanguage(languageCode);
        LocaleHelper.setLocale(requireContext(), languageCode);
        requireActivity().recreate();
    }
}

