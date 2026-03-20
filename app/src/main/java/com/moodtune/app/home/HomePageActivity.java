package com.moodtune.app.home;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.moodtune.app.R;
import com.moodtune.app.auth.LoginActivity;
import com.moodtune.app.chatbot.ChatbotActivity;
import com.moodtune.app.database.MoodDatabaseHandler;
import com.moodtune.app.face.FaceDetectionActivity;
import com.moodtune.app.music.HomeActivity;
import com.moodtune.app.reports.DailyReportActivity;
import com.moodtune.app.reports.MonthlyReportActivity;
import com.moodtune.app.reports.WeeklyReportActivity;
import com.moodtune.app.settings.SettingsActivity;

public class HomePageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView txtWelcome, txtUserName;
    private Button btnFaceDetection, btnChatbot;
    private Button btnHappy, btnSad, btnAngry, btnNeutral, btnSurprise;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MoodDatabaseHandler moodDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        moodDatabaseHandler = new MoodDatabaseHandler();

        // Check if user is logged in
        if (currentUser == null) {
            Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
        updateWelcomeMessage();
        animateWelcomeTexts();
        animateEmojiButtons();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtUserName = findViewById(R.id.txtUserName);
        btnFaceDetection = findViewById(R.id.btnFaceDetection);
        btnChatbot = findViewById(R.id.btnChatbot);
        btnHappy = findViewById(R.id.btnHappy);
        btnSad = findViewById(R.id.btnSad);
        btnAngry = findViewById(R.id.btnAngry);
        btnNeutral = findViewById(R.id.btnNeutral);
        btnSurprise = findViewById(R.id.btnSurprise);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        
        // Setup drawer toggle
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.menu_weekly_summary) {
                startActivity(new Intent(HomePageActivity.this, WeeklyReportActivity.class));
            } else if (itemId == R.id.menu_mood_chart) {
                startActivity(new Intent(HomePageActivity.this, DailyReportActivity.class));
            } else if (itemId == R.id.menu_mood_history) {
                startActivity(new Intent(HomePageActivity.this, MonthlyReportActivity.class));
            } else if (itemId == R.id.menu_settings) {
                startActivity(new Intent(HomePageActivity.this, SettingsActivity.class));
            }
            
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupClickListeners() {
        // Face Detection Button
        btnFaceDetection.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, FaceDetectionActivity.class);
            startActivity(intent);
        });

        // Chatbot Button (bottom)
        btnChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        // Quick Mood Buttons
        btnHappy.setOnClickListener(v -> handleQuickMood("happy"));
        btnSad.setOnClickListener(v -> handleQuickMood("sad"));
        btnAngry.setOnClickListener(v -> handleQuickMood("angry"));
        btnNeutral.setOnClickListener(v -> handleQuickMood("neutral"));
        btnSurprise.setOnClickListener(v -> handleQuickMood("surprise"));
    }

    private void handleQuickMood(String mood) {
        // Save mood to database
        moodDatabaseHandler.saveMoodFromQuickButton(mood);
        
        // Show toast
        Toast.makeText(this, "Mood saved: " + mood, Toast.LENGTH_SHORT).show();
        
        // Navigate to song recommendation
        Intent intent = new Intent(HomePageActivity.this, HomeActivity.class);
        intent.putExtra("selected_mood", mood);
        startActivity(intent);
    }

    private void updateWelcomeMessage() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            
            // Set "Welcome" with serif font
            SpannableString welcomeSpannable = new SpannableString("Welcome");
            welcomeSpannable.setSpan(new TypefaceSpan("serif"), 0, "Welcome".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            welcomeSpannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, "Welcome".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtWelcome.setText(welcomeSpannable);
            
            // Set user name with same serif font below
            if (displayName != null && !displayName.isEmpty()) {
                SpannableString nameSpannable = new SpannableString(displayName + "!");
                nameSpannable.setSpan(new TypefaceSpan("serif"), 0, nameSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                nameSpannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, nameSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtUserName.setText(nameSpannable);
                txtUserName.setVisibility(View.VISIBLE);
            } else {
                txtUserName.setVisibility(View.GONE);
            }
        }
    }
    
    private void animateWelcomeTexts() {
        // Set initial state - invisible and translated up
        txtWelcome.setAlpha(0f);
        txtWelcome.setTranslationY(-50f);
        txtUserName.setAlpha(0f);
        txtUserName.setTranslationY(-50f);
        
        // Animate "Welcome" first
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            txtWelcome.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }, 200);
        
        // Animate user name after welcome
        handler.postDelayed(() -> {
            txtUserName.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }, 600);
    }

    private void animateEmojiButtons() {
        // Get all emoji card views
        View[] emojiCards = {
            findViewById(R.id.cardHappy),
            findViewById(R.id.cardSad),
            findViewById(R.id.cardAngry),
            findViewById(R.id.cardNeutral),
            findViewById(R.id.cardSurprise)
        };

        // Set initial alpha to 0
        for (View card : emojiCards) {
            card.setAlpha(0f);
        }

        // Animate each emoji button with staggered delay
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < emojiCards.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                View card = emojiCards[index];
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(600);
                fadeIn.setFillAfter(true);
                card.startAnimation(fadeIn);
                card.setAlpha(1f);
            }, 100 + (i * 150)); // Staggered delay: 100ms, 250ms, 400ms, 550ms, 700ms
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

