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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved settings from SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT);

        // Apply default theme (Light Mode)
        if (savedTheme.equals(THEME_DARK)) {
            setTheme(R.style.Theme_QuickChat_Dark);
        } else {
            setTheme(R.style.Theme_QuickChat_Light);
        }

        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        initializeFields();

        // Load saved settings
        loadSavedSettings();

        // Handle language selection
        languageVietnameseLayout.setOnClickListener(v -> selectLanguage(LANGUAGE_VIETNAMESE));
        languageEnglishLayout.setOnClickListener(v -> selectLanguage(LANGUAGE_ENGLISH));
        languageVietnameseCheckbox.setOnClickListener(v -> selectLanguage(LANGUAGE_VIETNAMESE));
        languageEnglishCheckbox.setOnClickListener(v -> selectLanguage(LANGUAGE_ENGLISH));

        // Handle theme selection
        themeLightLayout.setOnClickListener(v -> selectTheme(THEME_LIGHT));
        themeDarkLayout.setOnClickListener(v -> selectTheme(THEME_DARK));
        themeLightCheckbox.setOnClickListener(v -> selectTheme(THEME_LIGHT));
        themeDarkCheckbox.setOnClickListener(v -> selectTheme(THEME_DARK));

        // Handle save button click
        saveButton.setOnClickListener(v -> saveSettings());

        // Handle back button click
        backButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HomeScreenActivity.class)));
    }

    private void initializeFields() {
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
    }

    private void loadSavedSettings() {
        // Default language is Vietnamese, default theme is Light Mode
        String savedLanguage = sharedPreferences.getString(PREF_LANGUAGE, LANGUAGE_VIETNAMESE);
        String savedTheme = sharedPreferences.getString(PREF_THEME, THEME_LIGHT);

        // Update UI based on saved settings
        if (savedLanguage.equals(LANGUAGE_VIETNAMESE)) {
            selectLanguage(LANGUAGE_VIETNAMESE);
        } else {
            selectLanguage(LANGUAGE_ENGLISH);
        }

        if (savedTheme.equals(THEME_LIGHT)) {
            selectTheme(THEME_LIGHT);
        } else {
            selectTheme(THEME_DARK);
        }
    }

    private void selectLanguage(String language) {
        // Uncheck all language checkboxes
        languageVietnameseCheckbox.setChecked(false);
        languageEnglishCheckbox.setChecked(false);

        if (language.equals(LANGUAGE_VIETNAMESE)) {
            languageVietnameseCheckbox.setChecked(true);
        } else {
            languageEnglishCheckbox.setChecked(true);
        }
    }

    private void selectTheme(String theme) {
        // Uncheck all theme checkboxes
        themeLightCheckbox.setChecked(false);
        themeDarkCheckbox.setChecked(false);

        if (theme.equals(THEME_LIGHT)) {
            themeLightCheckbox.setChecked(true);
        } else {
            themeDarkCheckbox.setChecked(true);
        }
    }

    private void saveSettings() {
        String selectedLanguage = languageVietnameseCheckbox.isChecked() ? LANGUAGE_VIETNAMESE : LANGUAGE_ENGLISH;
        String selectedTheme = themeLightCheckbox.isChecked() ? THEME_LIGHT : THEME_DARK;

        // Save settings to SharedPreferences
        editor = sharedPreferences.edit();
        editor.putString(PREF_LANGUAGE, selectedLanguage);
        editor.putString(PREF_THEME, selectedTheme);
        editor.apply();

        // Apply new settings
        updateLocale(selectedLanguage);
        applyTheme(selectedTheme);

        // Refresh UI
        recreate();

        Toast.makeText(this, "Cài đặt đã được lưu", Toast.LENGTH_SHORT).show();
    }

    private void updateLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void applyTheme(String theme) {
        if (theme.equals(THEME_DARK)) {
            setTheme(R.style.Theme_QuickChat_Dark); // Tên theme bạn đặt trong styles.xml
        } else {
            setTheme(R.style.Theme_QuickChat_Light);
        }
    }
}