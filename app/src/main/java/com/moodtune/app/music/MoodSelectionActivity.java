package com.moodtune.app.music;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.moodtune.app.R;
import com.moodtune.app.database.MoodDatabaseHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MoodSelectionActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private MoodDatabaseHandler moodDatabaseHandler;
    
    private Button btnHappy, btnSad, btnAngry, btnSurprise, btnNeutral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        moodDatabaseHandler = new MoodDatabaseHandler();

        // If user is not signed in, redirect to login
        if (currentUser == null) {
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnHappy = findViewById(R.id.btnHappy);
        btnSad = findViewById(R.id.btnSad);
        btnAngry = findViewById(R.id.btnAngry);
        btnSurprise = findViewById(R.id.btnSurprise);
        btnNeutral = findViewById(R.id.btnNeutral);
    }

    private void setupClickListeners() {
        btnHappy.setOnClickListener(v -> selectMood("happy"));
        btnSad.setOnClickListener(v -> selectMood("sad"));
        btnAngry.setOnClickListener(v -> selectMood("angry"));
        btnSurprise.setOnClickListener(v -> selectMood("surprise"));
        btnNeutral.setOnClickListener(v -> selectMood("neutral"));
    }

    private void selectMood(String mood) {
        if (currentUser != null) {
            // Use unified database handler
            moodDatabaseHandler.saveMoodFromQuickButton(mood);
            
            // Also update user document
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("currentMood", mood);
            userUpdate.put("isFirstTime", false);
            userDoc.set(userUpdate, SetOptions.merge());

            Toast.makeText(this, "Mood selected: " + mood, Toast.LENGTH_SHORT).show();

            // Navigate to home activity - suggested songs will be loaded there
            Intent intent = new Intent(MoodSelectionActivity.this, HomeActivity.class);
            intent.putExtra("selected_mood", mood);
            startActivity(intent);
            finish();
        }
    }
}

