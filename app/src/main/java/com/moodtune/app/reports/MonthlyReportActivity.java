package com.moodtune.app.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.Entry;
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

public class MonthlyReportActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private BarChart monthlyChart;
    private FirebaseFirestore db;
    private String userId;
    private final String[] moods = {"😊", "😲", "😐", "😔", "😡"}; // Happy, Surprise, Neutral, Sad, Angry
    private final String[] allMonths = new DateFormatSymbols().getMonths();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        spinnerMonth = findViewById(R.id.spinnerMonth);
        monthlyChart = findViewById(R.id.monthlyChart);
        db = FirebaseFirestore.getInstance();

        setupMonthSpinner();
        setupChartAppearance();
        setupChartInteraction();
    }

    private void setupMonthSpinner() {
        // Remove spinner - we're showing today's data only
        spinnerMonth.setVisibility(View.GONE);
        fetchTodaysMoodData();
    }

    private void fetchTodaysMoodData() {
        DocumentReference moodRef = db.collection("moods").document(userId);

        moodRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                updateMonthlyChart(new HashMap<>());
                return;
            }

            List<Map<String, Object>> history = (List<Map<String, Object>>) document.get("history");
            if (history == null || history.isEmpty()) {
                updateMonthlyChart(new HashMap<>());
                return;
            }

            // Get today's date in UTC
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String todayStr = sdf.format(today.getTime());

            // Count ALL moods detected today (no priority filtering)
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
                
                // Only count today's moods
                if (onlyDate.equals(todayStr)) {
                    String mood = (String) entry.get("mood");
                    if (mood != null) {
                        String moodLower = mood.toLowerCase(Locale.ROOT);
                        if (moodCounts.containsKey(moodLower)) {
                            moodCounts.put(moodLower, moodCounts.get(moodLower) + 1);
                        }
                    }
                }
            }

            Log.d("MonthlyReport", "Today's mood counts: " + moodCounts.toString());
            updateMonthlyChart(moodCounts);
        }).addOnFailureListener(e -> Log.e("MonthlyReport", "Error fetching moods", e));
    }



    private void updateMonthlyChart(Map<String, Integer> moodCounts) {
        List<BarEntry> entries = new ArrayList<>();
        
        // Map mood names to their indices
        String[] moodNames = {"happy", "surprise", "neutral", "sad", "angry"};
        int[] moodColors = {
            0xFF4CAF50,  // Green for happy
            0xFF2196F3,  // Blue for surprise
            0xFF9E9E9E,  // Gray for neutral
            0xFFFFC107,  // Yellow for sad
            0xFFF44336   // Red for angry
        };

        int totalCount = 0;
        for (String mood : moodNames) {
            totalCount += moodCounts.getOrDefault(mood, 0);
        }

        for (int i = 0; i < moods.length; i++) {
            String moodName = moodNames[i];
            int count = moodCounts.getOrDefault(moodName, 0);
            int percentage = totalCount > 0 ? Math.round((count * 100.0f) / totalCount) : 0;
            entries.add(new BarEntry(i, percentage));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Mood Distribution");
        dataSet.setColors(moodColors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        monthlyChart.setData(barData);
        monthlyChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(moods));
        monthlyChart.invalidate();
        monthlyChart.animateY(1000);
    }

    private void setupChartAppearance() {
        monthlyChart.setDrawGridBackground(false);
        monthlyChart.setDrawBarShadow(false);
        monthlyChart.setDrawValueAboveBar(false);
        monthlyChart.setPinchZoom(false);
        monthlyChart.setScaleEnabled(false);
        monthlyChart.setBackgroundColor(0xFFFFFFFF);

        XAxis xAxis = monthlyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(24f); // Increased emoji size
        xAxis.setYOffset(20f); // Increased offset to push emojis down and prevent cutoff
        xAxis.setLabelRotationAngle(0f); // Keep labels horizontal
        // Ensure labels are not clipped
        monthlyChart.setExtraBottomOffset(30f); // Extra space at bottom for emojis

        YAxis leftAxis = monthlyChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextSize(14f);

        monthlyChart.getAxisRight().setEnabled(false);
        monthlyChart.getDescription().setEnabled(true);
        monthlyChart.getDescription().setText("Mood Percentage Distribution");
        monthlyChart.getDescription().setTextSize(12f);
        monthlyChart.getDescription().setPosition(0f, 0f);
        monthlyChart.getLegend().setEnabled(false);
    }

    private void setupChartInteraction() {
        monthlyChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                float value = e.getY();
                String mood = moods[index];
                Toast.makeText(MonthlyReportActivity.this,
                        mood + " : " + value + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {}
        });
    }
}

