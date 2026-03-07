package com.tngoc.familytaskapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        saveLanguage(context, languageCode);
        return updateResources(context, languageCode);
    }

    public static Context onAttach(Context context) {
        String language = getSavedLanguage(context, Locale.getDefault().getLanguage());
        return updateResources(context, language);
    }

    public static String getSavedLanguage(Context context, String defaultLanguage) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.PREF_LANGUAGE, defaultLanguage);
    }

    private static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.PREF_LANGUAGE, languageCode).apply();
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}

