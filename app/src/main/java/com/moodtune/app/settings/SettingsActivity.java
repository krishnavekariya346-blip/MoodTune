package com.moodtune.app.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.moodtune.app.MoodTuneApplication;
import com.moodtune.app.R;
import com.moodtune.app.auth.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout layoutEditProfile, layoutChangePassword, layoutTheme, layoutAbout, layoutSignOut;
    private TextView tvUserName;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        initializeViews();
        setupUserInfo();
        setupClickListeners();
    }

    private void initializeViews() {
        layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutAbout = findViewById(R.id.layoutAbout);
        layoutSignOut = findViewById(R.id.layoutSignOut);
        tvUserName = findViewById(R.id.tvUserName);
    }

    private void setupUserInfo() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                String email = currentUser.getEmail();
                if (email != null && !email.isEmpty()) {
                    // Extract name from email (part before @)
                    String name = email.substring(0, email.indexOf("@"));
                    tvUserName.setText(name);
                } else {
                    tvUserName.setText("User");
                }
            }
        }
    }

    private void setupClickListeners() {
        layoutEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        layoutTheme.setOnClickListener(v -> {
            showThemeDialog();
        });

        layoutAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        layoutSignOut.setOnClickListener(v -> {
            signOut();
        });
    }

    private void signOut() {
        mAuth.signOut();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            SharedPreferences prefs = getSharedPreferences("MoodTunePrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showThemeDialog() {
        int currentTheme = MoodTuneApplication.getThemeMode(getApplication());
        int selectedIndex = 0;
        
        // Determine which option is currently selected
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            selectedIndex = 1; // Dark mode
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            selectedIndex = 0; // Light mode
        }
        
        String[] themes = {
            getString(R.string.light_mode),
            getString(R.string.dark_mode)
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_theme);
        builder.setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
            int themeMode;
            if (which == 0) {
                // Light mode
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                // Dark mode
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            
            // Save theme preference
            MoodTuneApplication.saveThemeMode(getApplication(), themeMode);
            
            // Apply theme immediately
            MoodTuneApplication.setThemeMode(themeMode);
            
            // Show message
            Toast.makeText(this, R.string.theme_changed, Toast.LENGTH_SHORT).show();
            
            dialog.dismiss();
            
            // Restart activity to apply theme changes
            recreate();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

