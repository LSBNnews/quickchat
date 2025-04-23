package com.example.quickchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "QuickChatPrefs";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_THEME = "theme";

    // Language options
    private static final String LANGUAGE_VIETNAMESE = "vi";
    private static final String LANGUAGE_ENGLISH = "en";

    // Theme options
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";

    // UI elements
    private LinearLayout languageVietnameseLayout, languageEnglishLayout;
    private CheckBox languageVietnameseCheckbox, languageEnglishCheckbox;
    private ImageView flagVietnamese, flagEnglish;
    private TextView languageVietnamese, languageEnglish;

    private LinearLayout themeLightLayout, themeDarkLayout;
    private CheckBox themeLightCheckbox, themeDarkCheckbox;
    private TextView themeLight, themeDark;

    private Button saveButton, backButton;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Flag để tránh trigger listener khi đang cập nhật checkbox
    private boolean isUpdatingThemeCheckboxes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        languageVietnameseLayout = findViewById(R.id.settings_language_vietnamese_layout);
        languageEnglishLayout = findViewById(R.id.settings_language_english_layout);
        languageVietnameseCheckbox = findViewById(R.id.settings_language_vietnamese_checkbox);
        languageEnglishCheckbox = findViewById(R.id.settings_language_english_checkbox);
        flagVietnamese = findViewById(R.id.settings_flag_vietnamese);
        flagEnglish = findViewById(R.id.settings_flag_english);
        languageVietnamese = findViewById(R.id.settings_language_vietnamese);
        languageEnglish = findViewById(R.id.settings_language_english);

        themeLightLayout = findViewById(R.id.settings_theme_light_layout);
        themeDarkLayout = findViewById(R.id.settings_theme_dark_layout);
        themeLightCheckbox = findViewById(R.id.settings_theme_light_checkbox);
        themeDarkCheckbox = findViewById(R.id.settings_theme_dark_checkbox);
        themeLight = findViewById(R.id.settings_theme_light);
        themeDark = findViewById(R.id.settings_theme_dark);

        saveButton = findViewById(R.id.settings_save_button);
        backButton = findViewById(R.id.settings_back_button);

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load saved preferences
        loadPreferences();

        // Theme checkboxes listeners
        themeLightCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingThemeCheckboxes) return;
            if (isChecked) {
                isUpdatingThemeCheckboxes = true;
                themeDarkCheckbox.setChecked(false);
                setTheme(THEME_LIGHT);
                isUpdatingThemeCheckboxes = false;
            }
        });

        themeDarkCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingThemeCheckboxes) return;
            if (isChecked) {
                isUpdatingThemeCheckboxes = true;
                themeLightCheckbox.setChecked(false);
                setTheme(THEME_DARK);
                isUpdatingThemeCheckboxes = false;
            }
        });

        // Back button
        backButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HomeScreenActivity.class)));

        // Save button
        saveButton.setOnClickListener(v -> savePreferences());
    }

    private void loadPreferences() {
        String theme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT);
        isUpdatingThemeCheckboxes = true;
        if (THEME_LIGHT.equals(theme)) {
            themeLightCheckbox.setChecked(true);
            themeDarkCheckbox.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            themeLightCheckbox.setChecked(false);
            themeDarkCheckbox.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        isUpdatingThemeCheckboxes = false;
    }

    private void setTheme(String theme) {
        String currentTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT);
        if (!theme.equals(currentTheme)) {
            editor.putString(PREF_THEME, theme).apply();
            if (THEME_LIGHT.equals(theme)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
    }

    private void savePreferences() {
        Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
