package com.moodtune.app.face;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moodtune.app.R;
import com.moodtune.app.database.MoodDatabaseHandler;
import com.moodtune.app.music.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceDetectionActivity extends AppCompatActivity {

    private String getBackendUrl() {
        if (isRunningOnEmulator()) {
            return "http://10.0.2.2:5000/analyze";
        } else {
            return "http://10.59.109.122:5000/analyze";
        }
    }

    private boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView tvStatusSubtitle;
    private TextView tvProgressText;
    private LinearLayout layoutProgress;
    private ImageView ivStatusIcon;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MoodDatabaseHandler moodDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_detection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moodDatabaseHandler = new MoodDatabaseHandler();
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        tvStatusSubtitle = findViewById(R.id.tvStatusSubtitle);
        tvProgressText = findViewById(R.id.tvProgressText);
        layoutProgress = findViewById(R.id.layoutProgress);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        FrameLayout captureButton = findViewById(R.id.capture_button);

        setupLaunchers();

        captureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void setupLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap != null) {
                                analyzeEmotion(bitmap);
                            } else {
                                showError("Unable to capture image.");
                            }
                        } else {
                            showError("Unable to capture image.");
                        }
                    }
                }
        );
    }

    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            showError("No camera app available.");
        }
    }

    private void analyzeEmotion(Bitmap bitmap) {
        layoutProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Analyzing emotion...");
        tvStatusSubtitle.setText("Please wait while we analyze your photo");
        tvProgressText.setText("Analyzing emotion...");
        ivStatusIcon.setVisibility(View.GONE);

        executor.submit(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
            Request request = new Request.Builder()
                    .url(getBackendUrl())
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    onAnalysisFailed("Server error: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                String emotion = json.optString("emotion", "unknown");
                double confidence = json.optDouble("confidence", 0.0);
                
                onAnalysisSuccess(emotion, confidence);
            } catch (IOException | JSONException e) {
                onAnalysisFailed(e.getMessage());
            }
        });
    }

    private void onAnalysisSuccess(String emotion, double confidence) {
        runOnUiThread(() -> {
            layoutProgress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Emotion detected: " + emotion);
            tvStatusSubtitle.setText("Confidence: " + String.format("%.0f%%", confidence * 100));
            ivStatusIcon.setVisibility(View.VISIBLE);
            
            // Save mood to database
            moodDatabaseHandler.saveMoodFromFace(emotion, confidence);
            
            Toast.makeText(this, "Mood detected: " + emotion, Toast.LENGTH_SHORT).show();
            
            // Navigate to song recommendation
            Intent intent = new Intent(FaceDetectionActivity.this, HomeActivity.class);
            intent.putExtra("selected_mood", emotion);
            startActivity(intent);
            finish();
        });
    }

    private void onAnalysisFailed(String message) {
        runOnUiThread(() -> {
            layoutProgress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Ready");
            tvStatusSubtitle.setText("Tap the button below to capture your photo");
            ivStatusIcon.setVisibility(View.VISIBLE);
            showError(message != null ? message : "Failed to analyze emotion.");
        });
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}

