package com.moodtune.app.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moodtune.app.R;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WeeklyReportActivity extends AppCompatActivity {

    PieChart weekPieChart;
    TextView tvMonth;
    Spinner monthSpinner;
    FirebaseFirestore db;
    String userId;
    int[] weekColors = {
            Color.parseColor("#FF6F61"),
            Color.parseColor("#6A67CE"),
            Color.parseColor("#20B2AA"),
            Color.parseColor("#FFD700")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        weekPieChart = findViewById(R.id.weekPieChart);
        tvMonth = findViewById(R.id.tvMonth);
        monthSpinner = findViewById(R.id.monthSpinner);
        db = FirebaseFirestore.getInstance();

        // Hide spinner - we're showing weekly data
        monthSpinner.setVisibility(View.GONE);
        tvMonth.setText("This Week's Mood Distribution");
        fetchWeeklyMoods();
    }

    private void fetchWeeklyMoods() {
        DocumentReference moodRef = db.collection("moods").document(userId);

        moodRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                setupWeekPieChart(new HashMap<>());
                return;
            }

            List<Map<String, Object>> history = (List<Map<String, Object>>) document.get("history");
            if (history == null || history.isEmpty()) {
                setupWeekPieChart(new HashMap<>());
                return;
            }

            // Get the date range for the last 7 days
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar weekAgo = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            weekAgo.add(Calendar.DAY_OF_YEAR, -6); // Last 7 days including today
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String todayStr = sdf.format(today.getTime());
            String weekAgoStr = sdf.format(weekAgo.getTime());

            // Count ALL moods detected in the last 7 days (no priority filtering)
            Map<String, Integer> moodCounts = new HashMap<>();
            moodCounts.put("happy", 0);
            moodCounts.put("sad", 0);
            moodCounts.put("angry", 0);
            moodCounts.put("neutral", 0);
            moodCounts.put("surprise", 0);

            for (Map<String, Object> entry : history) {
                String dateStr = (String) entry.get("date");
                if (dateStr == null || dateStr.length() < 10) {
                    continue;
                }
                String onlyDate = dateStr.substring(0, 10);
                
                // Only count moods from the last 7 days
                if (onlyDate.compareTo(weekAgoStr) >= 0 && onlyDate.compareTo(todayStr) <= 0) {
                    String mood = (String) entry.get("mood");
                    if (mood != null) {
                        String moodLower = mood.toLowerCase(Locale.ROOT);
                        if (moodCounts.containsKey(moodLower)) {
                            moodCounts.put(moodLower, moodCounts.get(moodLower) + 1);
                        }
                    }
                }
            }

            Log.d("WeeklyReport", "Weekly mood counts: " + moodCounts.toString());
            setupWeekPieChart(moodCounts);
        }).addOnFailureListener(e -> Log.e("WeeklyReport", "Error fetching moods", e));
    }


    private void setupWeekPieChart(Map<String, Integer> moodCounts) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Define all moods with their emojis and colors
        String[] moods = {"happy", "surprise", "neutral", "sad", "angry"};
        String[] moodLabels = {"😊 Happy", "😲 Surprise", "😐 Neutral", "😔 Sad", "😡 Angry"};
        int[] moodColors = {
            Color.parseColor("#4CAF50"),  // Green for happy
            Color.parseColor("#2196F3"),  // Blue for surprise
            Color.parseColor("#9E9E9E"),  // Gray for neutral
            Color.parseColor("#FFC107"),  // Yellow for sad
            Color.parseColor("#F44336")   // Red for angry
        };

        int totalCount = 0;
        for (String mood : moods) {
            int count = moodCounts.getOrDefault(mood, 0);
            if (count > 0) {
                entries.add(new PieEntry(count, moodLabels[getMoodIndex(mood)]));
                colors.add(moodColors[getMoodIndex(mood)]);
                totalCount += count;
            }
        }

        // If no data, show empty state
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
                colors.add(Color.LTGRAY);
        }

        // Make totalCount final for use in inner class
        final int finalTotalCount = totalCount;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (finalTotalCount > 0) {
                    int percentage = Math.round((value / finalTotalCount) * 100);
                    return percentage + "%";
                }
                return "";
            }
        });

        PieData pieData = new PieData(dataSet);
        weekPieChart.setData(pieData);
        weekPieChart.setDrawHoleEnabled(true);
        weekPieChart.setHoleRadius(45f);
        weekPieChart.setTransparentCircleRadius(50f);
        weekPieChart.setEntryLabelTextSize(14f);
        weekPieChart.setEntryLabelColor(Color.BLACK);
        weekPieChart.getDescription().setEnabled(true);
        weekPieChart.getDescription().setText("Mood Distribution for Selected Month");
        weekPieChart.getDescription().setTextSize(12f);
        weekPieChart.getDescription().setPosition(0f, 0f);
        com.github.mikephil.charting.components.Legend legend = weekPieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(11f);
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDirection(com.github.mikephil.charting.components.Legend.LegendDirection.LEFT_TO_RIGHT);
        // Configure legend to wrap into multiple rows - reduce spacing to fit better
        legend.setWordWrapEnabled(true);
        legend.setFormSize(7f); // Smaller form size
        legend.setXEntrySpace(6f); // Reduced horizontal spacing
        legend.setYEntrySpace(8f); // Vertical spacing between rows
        legend.setFormToTextSpace(4f); // Space between color square and text
        // Remove horizontal offset to minimize left padding
        legend.setXOffset(0f);
        legend.setYOffset(8f);
        weekPieChart.animateY(1000);
        weekPieChart.invalidate();
    }

    private int getMoodIndex(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return 0;
            case "surprise": return 1;
            case "neutral": return 2;
            case "sad": return 3;
            case "angry": return 4;
            default: return 2;
        }
    }
}

