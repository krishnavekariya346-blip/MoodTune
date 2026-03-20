package com.moodtune.app.chatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moodtune.app.R;
import com.moodtune.app.database.MoodDatabaseHandler;
import com.moodtune.app.models.ChatMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatbotActivity extends AppCompatActivity {
    RecyclerView rv;
    EditText et;
    Button btn;
    ChatAdapter adapter;
    List<ChatMessage> messages = new ArrayList<>();

    String BASE_URL = getServerUrl();
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    Gson gson = new Gson();
    
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    String userId;
    private MoodDatabaseHandler moodDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Check if user is logged in (no login screen, but verify auth)
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();
        moodDatabaseHandler = new MoodDatabaseHandler();

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.rvMessages);
        et = findViewById(R.id.etMessage);
        btn = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Add welcome message
        addBotMessage("Welcome to MoodTune Chatbot! Share your feelings and I'll detect your mood.");

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String text = et.getText().toString().trim();
                if (text.isEmpty()) return;
                addUserMessage(text);
                et.setText("");
                sendToBackend(text);
            }
        });
    }

    private String getServerUrl() {
        boolean isEmulator = isRunningOnEmulator();
        if (isEmulator) {
            return "http://10.0.2.2:5001";
        } else {
            return "http://10.59.109.122:5001";
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

    private void addUserMessage(String t){
        messages.add(new ChatMessage(t, true));
        adapter.notifyItemInserted(messages.size()-1);
        rv.scrollToPosition(messages.size()-1);
    }

    private void addBotMessage(String t){
        messages.add(new ChatMessage(t, false));
        adapter.notifyItemInserted(messages.size()-1);
        rv.scrollToPosition(messages.size()-1);
    }

    private void sendToBackend(String text) {
        String url = BASE_URL + "/predict";
        Map<String,String> body = new HashMap<>();
        body.put("text", text);
        String json = gson.toJson(body);

        RequestBody rb = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request req = new Request.Builder().url(url).post(rb).build();

        client.newCall(req).enqueue(new Callback(){
            @Override public void onFailure(Call call, IOException e){
                runOnUiThread(() -> addBotMessage("Network error: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    runOnUiThread(() -> addBotMessage("Server error: " + response.code()));
                    return;
                }
                String resp = response.body().string();
                try {
                    JsonObject jo = gson.fromJson(resp, JsonObject.class);
                    String mood = jo.has("mood") ? jo.get("mood").getAsString() : "unknown";
                    String conf = jo.has("confidence") ? jo.get("confidence").getAsString() : "";
                    final String display = "Detected Mood: " + mood + (conf.isEmpty() ? "" : " (confidence: " + conf + ")");
                    runOnUiThread(() -> addBotMessage(display));
                    
                    // Save mood to Firestore using unified handler
                    moodDatabaseHandler.saveMoodFromChatbot(mood, conf);
                } catch (Exception ex) {
                    runOnUiThread(() -> addBotMessage("Parse error: " + ex.getMessage()));
                }
            }
        });
    }
}

