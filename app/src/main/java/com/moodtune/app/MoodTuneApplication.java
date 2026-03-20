package com.moodtune.app;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MoodTuneApplication extends Application {
    
    private static final String PREFS_NAME = "MoodTunePrefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    @Override
    public void onCreate() {
        super.onCreate();
        applyTheme();
    }
    
    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    
    public static void setThemeMode(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    
    public static int getThemeMode(Application app) {
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    
    public static void saveThemeMode(Application app, int mode) {
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
}

