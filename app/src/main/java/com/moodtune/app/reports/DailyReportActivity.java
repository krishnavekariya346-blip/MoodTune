package com.moodtune.app.reports;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moodtune.app.R;
import com.moodtune.app.reports.MoodDotDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DailyReportActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvMoodEmoji, tvMonthlyAverageLabel, tvMonthlyAverageEmoji;
    private Button btnMonthlyAverage;
    private final Map<CalendarDay, String> moodMap = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private List<Map<String, Object>> moodHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        calendarView = findViewById(R.id.calendarView);
        tvMoodEmoji = findViewById(R.id.tvMoodEmoji);
        btnMonthlyAverage = findViewById(R.id.btnMonthlyAverage);
        tvMonthlyAverageLabel = findViewById(R.id.tvMonthlyAverageLabel);
        tvMonthlyAverageEmoji = findViewById(R.id.tvMonthlyAverageEmoji);

        btnMonthlyAverage.setOnClickListener(v -> calculateMonthlyAverage());

        fetchMoodData();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            // Hide monthly average when selecting a new date
            tvMonthlyAverageLabel.setVisibility(View.GONE);
            tvMonthlyAverageEmoji.setVisibility(View.GONE);
            
            String emoji = moodMap.get(date);
            tvMoodEmoji.clearAnimation();

            if (emoji != null) {
                showAnimatedEmoji(emoji);
            } else {
                tvMoodEmoji.setVisibility(View.GONE);
            }
        });
    }

    private void fetchMoodData() {
        DocumentReference moodRef = db.collection("moods").document(userId);

        moodRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                return;
            }

            List<Map<String, Object>> history = (List<Map<String, Object>>) document.get("history");
            if (history == null || history.isEmpty()) {
                return;
            }
            
            // Store history for monthly average calculation
            moodHistory = history;

            // Group moods by date and count occurrences to find dominant mood
            Map<String, Map<String, Integer>> dailyMoodCounts = new HashMap<>();

            for (Map<String, Object> entry : history) {
                String dateStr = (String) entry.get("date");
                if (dateStr == null || dateStr.length() < 10) {
                    continue;
                }
                String onlyDate = dateStr.substring(0, 10);
                String mood = (String) entry.get("mood");
                
                if (mood == null) continue;
                String moodLower = mood.toLowerCase(Locale.ROOT);
                
                // Count each mood occurrence per day
                if (!dailyMoodCounts.containsKey(onlyDate)) {
                    dailyMoodCounts.put(onlyDate, new HashMap<>());
                }
                Map<String, Integer> dayCounts = dailyMoodCounts.get(onlyDate);
                dayCounts.put(moodLower, dayCounts.getOrDefault(moodLower, 0) + 1);
            }

            // Find dominant mood for each day (most detected mood)
            Map<String, String> dominantMoods = new HashMap<>();
            for (String dateStr : dailyMoodCounts.keySet()) {
                Map<String, Integer> dayCounts = dailyMoodCounts.get(dateStr);
                String dominantMood = null;
                int maxCount = 0;
                
                for (Map.Entry<String, Integer> entry : dayCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        dominantMood = entry.getKey();
                    }
                }
                
                if (dominantMood != null) {
                    dominantMoods.put(dateStr, dominantMood);
                }
            }

            // Add dominant moods to calendar
            for (String dateStr : dominantMoods.keySet()) {
                String mood = dominantMoods.get(dateStr);
                CalendarDay day = convertToCalendarDay(dateStr + "T00:00:00Z");
                if (day == null) continue;
                
                String emoji = moodToEmoji(mood);
                moodMap.put(day, emoji);
                int color = getMoodColor(emoji);
                calendarView.addDecorator(new MoodDotDecorator(day, color));
            }
        }).addOnFailureListener(e -> Log.e("MOOD_ERROR", "Error loading moods", e));
    }


    private String moodToEmoji(String mood) {
        if (mood == null) return "😐";
        switch (mood.toLowerCase(Locale.ROOT)) {
            case "happy": return "😊";
            case "surprise": return "😲";
            case "neutral": return "😐";
            case "sad": return "😔";
            case "angry": return "😡";
            default: return "😐";
        }
    }

    private CalendarDay convertToCalendarDay(String dateStr) {
        try {
            String onlyDate = dateStr.substring(0, 10);
            String[] parts = onlyDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            return CalendarDay.from(year, month, day);
        } catch (Exception e) {
            Log.e("DATE_PARSE_FIX", e.getMessage());
        }
        return null;
    }


    private int getMoodColor(String emoji) {
        switch (emoji) {
            case "😊": return 0xFF4CAF50;
            case "😲": return 0xFF2196F3;
            case "😐": return 0xFF9E9E9E;
            case "😔": return 0xFFFFC107;
            case "😡": return 0xFFF44336;
            default: return 0xFF9E9E9E;
        }
    }

    private void showAnimatedEmoji(String emoji) {
        tvMoodEmoji.setText(emoji);
        tvMoodEmoji.setVisibility(View.VISIBLE);

        ScaleAnimation scale = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(600);
        scale.setFillAfter(true);
        tvMoodEmoji.startAnimation(scale);
        // Keep emoji visible, don't auto-hide
    }

    private void calculateMonthlyAverage() {
        if (moodHistory == null || moodHistory.isEmpty()) {
            tvMonthlyAverageLabel.setText("No mood data available");
            tvMonthlyAverageLabel.setVisibility(View.VISIBLE);
            tvMonthlyAverageEmoji.setVisibility(View.GONE);
            return;
        }

        // Get current month and year
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int currentMonth = today.get(Calendar.MONTH);
        int currentYear = today.get(Calendar.YEAR);

        // Count all moods in the current month
        Map<String, Integer> monthlyMoodCounts = new HashMap<>();
        monthlyMoodCounts.put("happy", 0);
        monthlyMoodCounts.put("sad", 0);
        monthlyMoodCounts.put("angry", 0);
        monthlyMoodCounts.put("neutral", 0);
        monthlyMoodCounts.put("surprise", 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (Map<String, Object> entry : moodHistory) {
            String dateStr = (String) entry.get("date");
            if (dateStr == null || dateStr.length() < 10) {
                continue;
            }
            String onlyDate = dateStr.substring(0, 10);

            try {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTime(sdf.parse(onlyDate));

                int entryMonth = cal.get(Calendar.MONTH);
                int entryYear = cal.get(Calendar.YEAR);

                // Only count moods from the current month
                if (entryMonth == currentMonth && entryYear == currentYear) {
                    String mood = (String) entry.get("mood");
                    if (mood != null) {
                        String moodLower = mood.toLowerCase(Locale.ROOT);
                        if (monthlyMoodCounts.containsKey(moodLower)) {
                            monthlyMoodCounts.put(moodLower, monthlyMoodCounts.get(moodLower) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MonthlyAverage", "Error parsing date: " + onlyDate, e);
            }
        }

        // Find the mood with the highest count
        String dominantMood = null;
        int maxCount = 0;
        int totalCount = 0;

        for (Map.Entry<String, Integer> entry : monthlyMoodCounts.entrySet()) {
            totalCount += entry.getValue();
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantMood = entry.getKey();
            }
        }

        // Hide daily mood emoji when showing monthly average
        tvMoodEmoji.setVisibility(View.GONE);
        tvMoodEmoji.clearAnimation();
        
        // Display the result
        if (dominantMood != null && totalCount > 0) {
            String emoji = moodToEmoji(dominantMood);
            tvMonthlyAverageLabel.setText("Monthly Average Mood (" + totalCount + " total detections):");
            tvMonthlyAverageEmoji.setText(emoji);
            tvMonthlyAverageLabel.setVisibility(View.VISIBLE);
            tvMonthlyAverageEmoji.setVisibility(View.VISIBLE);
            
            // Animate the monthly average emoji
            ScaleAnimation scale = new ScaleAnimation(
                    0f, 1f, 0f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(600);
            scale.setFillAfter(true);
            tvMonthlyAverageEmoji.startAnimation(scale);
        } else {
            tvMonthlyAverageLabel.setText("No mood data for this month");
            tvMonthlyAverageLabel.setVisibility(View.VISIBLE);
            tvMonthlyAverageEmoji.setVisibility(View.GONE);
        }
    }
}

