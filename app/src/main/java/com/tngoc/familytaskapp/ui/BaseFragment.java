package com.tngoc.familytaskapp.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tngoc.familytaskapp.R;

public abstract class BaseFragment extends Fragment {

    private View loadingOverlay;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        
        // Thêm hiệu ứng vào khi hiện Fragment
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.tab_enter);
        view.startAnimation(fadeIn);

        // Tìm hoặc Inject Loading Overlay nếu cần
        setupLoadingOverlay(view);
    }

    private void setupLoadingOverlay(View view) {
        if (view instanceof ViewGroup) {
            loadingOverlay = view.findViewById(R.id.loadingOverlay);
        }
    }

    protected void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}