package com.tngoc.familytaskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private final SharedPreferences prefs;

    public SharedPrefManager(Context context) {
        this.prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(Constants.PREF_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.PREF_USER_ID, null);
    }

    public void saveLanguage(String languageCode) {
        prefs.edit().putString(Constants.PREF_LANGUAGE, languageCode).apply();
    }

    public String getLanguage() {
        return prefs.getString(Constants.PREF_LANGUAGE, "vi");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

