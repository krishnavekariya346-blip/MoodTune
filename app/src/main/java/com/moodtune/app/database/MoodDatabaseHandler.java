package com.moodtune.app.database;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Unified database handler for storing moods from all sources (Face, Chatbot, Quick Mood buttons)
 */
public class MoodDatabaseHandler {
    private static final String TAG = "MoodDatabaseHandler";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public MoodDatabaseHandler() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Save mood to database
     * @param mood The detected/selected mood
     * @param confidence Confidence score (optional, for Face detection)
     * @param source Source of mood (face, chatbot, quick_mood)
     */
    public void saveMood(String mood, Double confidence, String source) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in, cannot save mood");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userDoc = db.collection("users").document(userId);
        DocumentReference moodDoc = db.collection("moods").document(userId);

        // Create mood entry
        Map<String, Object> moodEntry = new HashMap<>();
        moodEntry.put("mood", mood);
        moodEntry.put("date", getCurrentUtcTimestamp());
        moodEntry.put("source", source);
        if (confidence != null) {
            moodEntry.put("confidence", confidence);
        }

        // Update user's current mood
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("currentMood", mood);
        userUpdate.put("isFirstTime", false);

        // Add to mood history
        Map<String, Object> historyUpdate = new HashMap<>();
        historyUpdate.put("history", FieldValue.arrayUnion(moodEntry));

        // Batch write
        WriteBatch batch = db.batch();
        batch.set(userDoc, userUpdate, SetOptions.merge());
        batch.set(moodDoc, historyUpdate, SetOptions.merge());

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Mood saved successfully: " + mood);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save mood", e);
            });
    }

    /**
     * Save mood from Face detection
     */
    public void saveMoodFromFace(String mood, double confidence) {
        saveMood(mood, confidence, "face");
    }

    /**
     * Save mood from Chatbot
     */
    public void saveMoodFromChatbot(String mood, String confidence) {
        Double conf = null;
        try {
            if (confidence != null && !confidence.isEmpty()) {
                conf = Double.parseDouble(confidence);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse confidence: " + confidence);
        }
        saveMood(mood, conf, "chatbot");
    }

    /**
     * Save mood from Quick Mood button
     */
    public void saveMoodFromQuickButton(String mood) {
        saveMood(mood, null, "quick_mood");
    }

    private String getCurrentUtcTimestamp() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormat.format(new Date());
    }
}

