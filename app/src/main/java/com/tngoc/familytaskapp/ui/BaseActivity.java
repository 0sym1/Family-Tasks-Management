package com.tngoc.familytaskapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tngoc.familytaskapp.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceLtrDirection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        forceLtrDirection();
    }

    protected void forceLtrDirection() {
        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            View content = decorView.findViewById(android.R.id.content);
            if (content != null) {
                content.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }
    }
}
